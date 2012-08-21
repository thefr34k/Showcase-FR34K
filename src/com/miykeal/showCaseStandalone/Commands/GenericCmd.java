package com.miykeal.showCaseStandalone.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.interfaces.Cmd;

/**
 * Copyright (C) 2011 Kellerkindt <kellerkindt@miykeal.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Sorklin <sorklin at gmail.com>
 */
abstract class GenericCmd implements Cmd {
    
	CommandSender 		cs; 		//Always populated
    String[] 			args; 		//Original args from CommandListener
    Player 				player; 	//Only populated if cs is a player.  Only throws an error if mustBeAPlayer is true.
    
    //Default the generic to must be executed by a player, and no minimum arguments.
    String 				permission 		= "";
    boolean 			mustBePlayer 	= true;
    int 				minArg 			= 0;
    
    ShowCaseStandalone 	scs;
    String 				next 			= Term.NEXT.get();
    
    public GenericCmd(CommandSender cs, String args[]){
        this.cs = cs;
        this.args = args;
        this.scs = ShowCaseStandalone.get();
    }
    
    protected boolean errorCheck() 
            throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
        
        //Try to cast it and only throw a exception if command must be executed as player.
        try {
            this.player = (Player)cs;
        } catch (Exception ex) {
            if(mustBePlayer){
            	cs.sendMessage(Term.ERROR_EXECUTE_AS_PLAYER.get());
                return true;
            }
        }
        
        if(!scs.hasPermission(cs, permission)){
            throw new InsufficientPermissionException();
        }
        
        if(args.length < minArg){
            throw new MissingOrIncorrectArgumentException();
        }
        
        return false;
    }
}
