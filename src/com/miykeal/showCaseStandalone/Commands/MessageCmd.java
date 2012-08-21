package com.miykeal.showCaseStandalone.Commands;

import org.bukkit.command.CommandSender;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Term;

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
public class MessageCmd extends GenericCmd {
    
    public MessageCmd(CommandSender cs, String args[]){
        super(cs, args);
        this.permission = Properties.permUse;
    }

    @Override
    public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
        if(errorCheck())
            return true;
        
        //This is the catch all message (meaning true or false isn't specified):
        String msg = (ShowCaseStandalone.pv.ignoreMessages(player)) 
                    ? Term.IGNORE_RECEIVE.get("ignoring")
                    : Term.IGNORE_RECEIVE.get("receiving");
        
        //Overwrite if args[1] = true or false
        if(args.length > 1)
            if(args[1].equalsIgnoreCase("ignore")){
                ShowCaseStandalone.pv.setIgnoreMessages(player, true);
                msg = Term.IGNORE_TRANSACTION.get("ignored");
            } else if (args[1].equalsIgnoreCase("receive")) {
                ShowCaseStandalone.pv.setIgnoreMessages(player, false);
                msg = Term.IGNORE_TRANSACTION.get("received");
            } //msg: changeIgnoreReceive %1 for both
        
        Messaging.send(player, msg);
        return true;
    }
}
