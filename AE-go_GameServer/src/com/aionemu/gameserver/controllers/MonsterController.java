/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.events.AttackEvent;
import com.aionemu.gameserver.ai.npcai.MonsterAi;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.stats.CreatureGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.CreatureLifeStats;
import com.aionemu.gameserver.model.gameobjects.stats.NpcLifeStats;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.DecayService;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 *
 */
public class MonsterController extends NpcController
{
	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.controllers.CreatureController#doDrop()
	 */
	@Override
	public void doDrop()
	{
		super.doDrop();
		dropService.registerDrop((Monster) getOwner());
		PacketSendUtility.broadcastPacket(this.getOwner(), new SM_LOOT_STATUS(this.getOwner().getObjectId(), 0));
	}

	@Override
	public void doReward(Creature creature)
	{
		super.doReward(creature);

		if(creature instanceof Player)
		{
			Player player = (Player) creature;
			//TODO may be introduce increaseExpBy method in PlayerCommonData
			long currentExp = player.getCommonData().getExp();

			long xpReward = StatFunctions.calculateSoloExperienceReward(player, getOwner());
			player.getCommonData().setExp(currentExp + xpReward);

			PacketSendUtility.sendPacket(player,SM_SYSTEM_MESSAGE.EXP(Long.toString(xpReward)));

		}
	}
	
	@Override
	public boolean onAttack(Creature creature)
	{
		super.onAttack(creature);
		
		Monster monster = getOwner();
		CreatureLifeStats<? extends Creature> lifeStats = monster.getLifeStats();

		if(lifeStats.isAlreadyDead())
		{
			return false;
		}
		
		MonsterAi monsterAi = monster.getAi();
		monsterAi.handleEvent(new AttackEvent(creature));
		
		return true;
	}
	
	public void attackTarget(int targetObjectId)
	{
		Monster monster = getOwner();
		MonsterAi monsterAi = monster.getAi();
		CreatureGameStats<? extends Creature> npcGameStats = monster.getGameStats();

		int attackType = 0; //TODO investigate attack types	(0 or 1)

		World world = monster.getActiveRegion().getWorld();
		//TODO refactor to possibility npc-npc fight
		Player player = (Player) world.findAionObject(targetObjectId);

		//TODO fix last attack - cause mob is already dead
		int damage = StatFunctions.calculateBaseDamageToTarget(monster, player);

		PacketSendUtility.broadcastPacket(player,
			new SM_ATTACK(monster.getObjectId(), player.getObjectId(),
				npcGameStats.getAttackCounter(), 274, attackType, damage), true);
		//wtf is 274 - invetigate


		boolean attackSuccess = player.getController().onAttack(monster);

		if(attackSuccess)
		{
			player.getLifeStats().reduceHp(damage);
			npcGameStats.increaseAttackCounter();
		}
		if(player.getLifeStats().isAlreadyDead())
		{
			monsterAi.setAiState(AIState.IDLE);
		}
	}

	@Override
	public void onDie()
	{
		super.onDie();
		
		MonsterAi monsterAi = getOwner().getAi();
		monsterAi.setAiState(AIState.NONE);
		
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(this.getOwner().getObjectId(), 13 , getOwner().getObjectId()));
		this.doDrop();

		//TODO change - now reward is given to target only
		Player target = (Player) this.getOwner().getTarget();
		this.doReward(target);
		
		if(decayTask == null)
		{
			RespawnService.getInstance().scheduleRespawnTask(this.getOwner());
			decayTask = DecayService.getInstance().scheduleDecayTask(this.getOwner());
		}	
		
		//deselect target at the end
		getOwner().setTarget(null);
	}

	@Override
	public void onRespawn()
	{
		this.decayTask = null;
		dropService.unregisterDrop(getOwner());
		this.getOwner().setLifeStats(new NpcLifeStats(getOwner()));
	}

	@Override
	public Monster getOwner()
	{
		return (Monster) super.getOwner();
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
}
