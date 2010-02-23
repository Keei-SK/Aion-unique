/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PrivateStore;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE_NAME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.inject.Inject;

/**
 * @author Simple
 * 
 */
public class PrivateStoreService
{
	@Inject
	ItemService	itemService;

	/**
	 * @param activePlayer
	 * @param itemObjId
	 * @param itemId
	 * @param itemAmount
	 * @param itemPrice
	 */
	public void addItem(Player activePlayer, int[] itemObjIds, int[] itemIds, int[] itemAmounts, int[] itemPrices)
	{
		/**
		 * Check if player already has a store, if not create one
		 */
		if(activePlayer.getStore() == null)
			createStore(activePlayer);

		/**
		 * Define store to make things easier
		 */
		PrivateStore store = activePlayer.getStore();

		/**
		 * Check if player owns itemObjId else don't add item
		 */
		for(int i = 0; i < itemObjIds.length; i++)
		{
			Item item = getItemByObjId(activePlayer, itemObjIds[i]);
			if(item != null)
			{
				if(!validateItem(item, itemIds[i], itemAmounts[i]) && itemPrices[i] < 0)
					return;
				/**
				 * Add item to private store
				 */
				TradeItem tradeItem = new TradeItem(item.getObjectId(), itemAmounts[i]);
				store.addItemToSell(tradeItem, itemPrices[i]);
			}
		}
	}

	/**
	 * A check isn't really needed.....
	 * 
	 * @return
	 */
	private boolean validateItem(Item item, int itemId, int itemAmount)
	{
		if(item.getItemTemplate().getItemId() != itemId || itemAmount > item.getItemCount())
			return false;
		return true;
	}

	/**
	 * This method will create the player's store
	 * 
	 * @param activePlayer
	 */
	private void createStore(Player activePlayer)
	{
		activePlayer.setStore(new PrivateStore(activePlayer));
		activePlayer.setState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, 28, 0, 0), true);
	}

	/**
	 * This method will destroy the player's store
	 * 
	 * @param activePlayer
	 */
	public void closePrivateStore(Player activePlayer)
	{
		activePlayer.setStore(null);
		activePlayer.unsetState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, 29, 0, 0), true);
	}

	/**
	 * This method will move the item to the new player and move kinah to item owner
	 */
	public void sellStoreItem(Player seller, Player buyer, TradeList tradeList)
	{
		/**
		 * 1. Check if we are busy with two valid participants
		 */
		if(!validateParticipants(seller, buyer))
			return;

		/**
		 * Define store to make life easier
		 */
		PrivateStore store = seller.getStore();

		/**
		 * 2. Load all item object id's and validate if seller really owns them
		 */
		tradeList = loadObjIds(seller, tradeList);
		if(tradeList == null)
			return; // Invalid items found or store was empty

		/**
		 * 3. Check free slots
		 */
		Storage inventory = buyer.getInventory();
		int freeSlots = inventory.getLimit() - inventory.getAllItems().size() + 1;
		if(freeSlots < tradeList.size())
			return; // TODO message

		/**
		 * Create total price and items
		 */
		int price = getTotalPrice(store, tradeList);

		/**
		 * Check if player has enough kinah and remove it
		 */
		if(getKinahAmount(buyer) > price)
		{
			/**
			 * Decrease kinah for buyer and Increase kinah for seller
			 */
			decreaseKinahAmount(buyer, price);
			increaseKinahAmount(seller, price);

			List<Item> newItems = new ArrayList<Item>();
			for(TradeItem tradeItem : tradeList.getTradeItems())
			{
				Item item = getItemByObjId(seller, tradeItem.getItemId());
				TradeItem storeItem = store.getTradeItemById(tradeItem.getItemId());
				if(item != null)
				{
					/**
					 * If the buyer wants to buy everything and it's everything in the sellers inventory
					 */
					if(item.getItemCount() == tradeItem.getCount())
					{
						removeItemFromPlayer(seller, store, item);
						buyer.getInventory().putToBag(item);
						newItems.add(item);
					}
					else
					{
						decreaseItemFromPlayer(seller, item, tradeItem);
						itemService.addItem(buyer, item.getItemTemplate().getItemId(), tradeItem.getCount(), false);
						
						if(storeItem.getCount() == tradeItem.getCount())
							store.removeItem(storeItem);							
					}
				}
			}

			/**
			 * Add item to buyer's inventory
			 */
			if(newItems.size() > 0)
				PacketSendUtility.sendPacket(buyer, new SM_INVENTORY_UPDATE(newItems));

			/**
			 * Remove item from store and check if last item
			 */
			if(store.getSoldItems().size() == 0)
				closePrivateStore(seller);
			return;
		}
	}

	/**
	 * Decrease item count and update inventory
	 * 
	 * @param seller
	 * @param item
	 */
	private void decreaseItemFromPlayer(Player seller, Item item, TradeItem tradeItem)
	{
		item.decreaseItemCount(tradeItem.getCount());
		PacketSendUtility.sendPacket(seller, new SM_UPDATE_ITEM(item));
		
		for(TradeItem storeItem : seller.getStore().getSoldItems().keySet())
		{
			if(storeItem.getItemId() == tradeItem.getItemId())
				storeItem.decreaseCount(tradeItem.getCount());
		}
	}

	/**
	 * Remove item from store if count reached 0
	 * 
	 * @param seller
	 * @param store
	 */
	private void removeItemFromPlayer(Player seller, PrivateStore store, Item item)
	{
		for(TradeItem tradeItem : store.getSoldItems().keySet())
		{
			if(tradeItem.getItemId() == item.getObjectId())
				store.removeItem(tradeItem);
		}
		seller.getInventory().removeFromBag(item, false);
		PacketSendUtility.sendPacket(seller, new SM_DELETE_ITEM(item.getObjectId()));
	}

	/**
	 * @param seller
	 * @param tradeList
	 * @return
	 */
	private TradeList loadObjIds(Player seller, TradeList tradeList)
	{
		PrivateStore store = seller.getStore();
		TradeList newTradeList = new TradeList();

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			int i = 0;
			for(TradeItem item : store.getSoldItems().keySet())
			{
				if(i == tradeItem.getItemId())
				{
					if(tradeItem.getCount() > item.getCount())
						return null;
					newTradeList.addPSItem(item.getItemId(), tradeItem.getCount());
				}
				i++;
			}
		}

		/**
		 * Check if player still owns items
		 */
		if(!validateBuyItems(seller, newTradeList))
			return null;

		return newTradeList;
	}

	/**
	 * @param player1
	 * @param player2
	 */
	private boolean validateParticipants(Player itemOwner, Player newOwner)
	{
		return itemOwner != null && newOwner != null && itemOwner.isOnline() && newOwner.isOnline();
	}

	/**
	 * @param tradeList
	 */
	private boolean validateBuyItems(Player seller, TradeList tradeList)
	{
		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			Item item = seller.getInventory().getItemByObjId(tradeItem.getItemId());

			// 1) don't allow to sell fake items;
			if(item == null)
				return false;
		}
		return true;
	}

	/**
	 * This method will return the amount of kinah of a player
	 * 
	 * @param newOwner
	 * @return
	 */
	private int getKinahAmount(Player player)
	{
		return player.getInventory().getKinahItem().getItemCount();
	}

	/**
	 * This method will decrease the kinah amount of a player
	 * 
	 * @param player
	 * @param price
	 */
	private void decreaseKinahAmount(Player player, int price)
	{
		player.getInventory().decreaseKinah(price);
	}

	/**
	 * This method will increase the kinah amount of a player
	 * 
	 * @param player
	 * @param price
	 */
	private void increaseKinahAmount(Player player, int price)
	{
		player.getInventory().increaseKinah(price);
	}

	/**
	 * This method will return the item in a inventory by object id
	 * 
	 * @param player
	 * @param itemObjId
	 * @return
	 */
	private Item getItemByObjId(Player seller, int itemObjId)
	{
		return seller.getInventory().getItemByObjId(itemObjId);
	}

	/**
	 * This method will return the total price of the tradelist
	 * 
	 * @param store
	 * @param tradeList
	 * @return
	 */
	private int getTotalPrice(PrivateStore store, TradeList tradeList)
	{
		int totalprice = 0;
		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			for(TradeItem item : store.getSoldItems().keySet())
			{
				if(item.getItemId() == tradeItem.getItemId())
					totalprice += store.getSoldItems().get(item)*tradeItem.getCount();
			}
		}
		return totalprice;
	}

	/**
	 * @param activePlayer
	 */
	public void openPrivateStore(Player activePlayer, String name)
	{
		if(name != null)
		{
			activePlayer.getStore().setStoreMessage(name);
			PacketSendUtility.broadcastPacket(activePlayer,
				new SM_PRIVATE_STORE_NAME(activePlayer.getObjectId(), name), true);
			// PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.PRIVATE_STORE_START());
		}
		else
		{
			PacketSendUtility.broadcastPacket(activePlayer, new SM_PRIVATE_STORE_NAME(activePlayer.getObjectId(), ""),
				true);
			// PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.PRIVATE_STORE_STOP());
		}
	}
}