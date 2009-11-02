/*
 * This file is part of aion-unique <aion-unique.com>.
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
package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.action.Actions;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.effect.Effects;


/**
 * @author ATracer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "skillTemplate", propOrder = {
    "startconditions",
    "useconditions",
    "effects",
    "actions"
})
public class SkillTemplate {

	protected Conditions startconditions;
    protected Conditions useconditions;
    protected Effects effects;
    protected Actions actions;
    @XmlAttribute(name = "skill_id", required = true)
    protected int skillId;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected SkillType type;
    @XmlAttribute(required = true)
    protected int level;
    @XmlAttribute(required = true)
    protected int duration;
    @XmlAttribute
    protected Integer cooldown;

    /**
     * Gets the value of the startconditions property.
     * 
     * @return
     *     possible object is
     *     {@link Conditions }
     *     
     */
    public Conditions getStartconditions() {
        return startconditions;
    }
    
    /**
     * Gets the value of the useconditions property.
     * 
     * @return
     *     possible object is
     *     {@link Conditions }
     *     
     */
    public Conditions getUseconditions() {
        return useconditions;
    }

    /**
     * Gets the value of the effects property.
     * 
     * @return
     *     possible object is
     *     {@link Effects }
     *     
     */
    public Effects getEffects() {
        return effects;
    }

    /**
     * Gets the value of the actions property.
     * 
     * @return
     *     possible object is
     *     {@link Actions }
     *     
     */
    public Actions getActions() {
        return actions;
    }

    /**
     * Gets the value of the skillId property.
     * 
     */
    public int getSkillId() {
        return skillId;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link SkillType }
     *     
     */
    public SkillType getType() {
        return type;
    }

    /**
     * Gets the value of the level property.
     * 
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the value of the duration property.
     * 
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Gets the value of the cooldown property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getCooldown() {
        if (cooldown == null) {
            return  0;
        } else {
            return cooldown;
        }
    }

}