/**
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

package com.aionemu.gameserver.configs;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.aionemu.commons.configuration.Property;

/**
 * @author Simple
 */
public class LegionConfig
{
	/**
	 * Logger for this class.
	 */
	protected static final Logger	log	= Logger.getLogger(LegionConfig.class);

	/**
	 * Announcement pattern (checked when announcement is being created)
	 */
	@Property(key = "gameserver.legion.pattern", defaultValue = "[a-zA-Z ]{2,16}")
	public static Pattern			LEGION_NAME_PATTERN;

	/**
	 * Self Intro pattern (checked when self intro is being changed)
	 */
	@Property(key = "gameserver.legion.selfintropattern", defaultValue = "[a-zA-Z]{2,25}")
	public static Pattern			SELF_INTRO_PATTERN;

	/**
	 * Nickname pattern (checked when nickname is being changed)
	 */
	@Property(key = "gameserver.legion.nicknamepattern", defaultValue = "[a-zA-Z]{2,14}")
	public static Pattern			NICKNAME_PATTERN;

	/**
	 * Announcement pattern (checked when announcement is being created)
	 */
	@Property(key = "gameserver.legion.announcementpattern", defaultValue = "[a-zA-Z .,]{2,120}")
	public static Pattern			ANNOUNCEMENT_PATTERN;

	/*
	 * Sets disband legion time
	 */
	@Property(key = "gameserver.legion.disbandtime", defaultValue = "86400")
	public static int				LEGION_DISBAND_TIME;

	/*
	 * Sets required difference between disband time and can can create legion again
	 */
	@Property(key = "gameserver.legion.disbanddifference", defaultValue = "604800")
	public static int				LEGION_DISBAND_DIFFERENCE;

	/*
	 * Sets required kinah to create a legion
	 */
	@Property(key = "gameserver.legion.creationrequiredkinah", defaultValue = "10000")
	public static int				LEGION_CREATE_REQUIRED_KINAH;

	/*
	 * Sets required kinah to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredkinah", defaultValue = "100000")
	public static int				LEGION_LEVEL2_REQUIRED_KINAH;

	/*
	 * Sets required kinah to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredkinah", defaultValue = "1000000")
	public static int				LEGION_LEVEL3_REQUIRED_KINAH;

	/*
	 * Sets required amount of members to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredmembers", defaultValue = "10")
	public static int				LEGION_LEVEL2_REQUIRED_MEMBERS;

	/*
	 * Sets required amount of members to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredmembers", defaultValue = "20")
	public static int				LEGION_LEVEL3_REQUIRED_MEMBERS;

	/*
	 * Sets required amount of abyss point to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredabysspoint", defaultValue = "0")
	public static int				LEGION_LEVEL2_REQUIRED_ABYSS_POINT;

	/*
	 * Sets required amount of abyss point to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredabysspoint", defaultValue = "20000")
	public static int				LEGION_LEVEL3_REQUIRED_ABYSS_POINT;

	/*
	 * Sets max members of a level 1 legion
	 */
	@Property(key = "gameserver.legion.level1maxmembers", defaultValue = "30")
	public static int				LEGION_LEVEL1_MAX_MEMBERS;

	/*
	 * Sets max members of a level 2 legion
	 */
	@Property(key = "gameserver.legion.level2maxmembers", defaultValue = "60")
	public static int				LEGION_LEVEL2_MAX_MEMBERS;

	/*
	 * Sets max members of a level 3 legion
	 */
	@Property(key = "gameserver.legion.level3maxmembers", defaultValue = "90")
	public static int				LEGION_LEVEL3_MAX_MEMBERS;
}