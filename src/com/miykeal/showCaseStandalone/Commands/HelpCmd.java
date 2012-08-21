package com.miykeal.showCaseStandalone.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

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
public class HelpCmd extends GenericCmd {
    
    public HelpCmd(CommandSender cs, String args[]){
        super(cs, args);
        this.mustBePlayer = true;
        this.permission = Properties.permUse;
    }
    
    @Override
    public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
        if(errorCheck())
            return true;

        /*
         * This needs to be fixed/rewritten after debugging new buy/sell/display routines.
         */
        String page;
        if(args.length < 2)
            page = "1";
        else
            page = args[1].toLowerCase();
        
        List<String> msg	= new ArrayList<String>();
        
        if(page.equalsIgnoreCase("admin")){
            if(scs.hasPermission(cs, Properties.permAdmin)){
            	msg.add(Term.HELP_TITLE_ADMIN.get());
            	msg.add(Term.HELP_ADMIN_1.get());
            	msg.add(Term.HELP_ADMIN_2.get());
            	msg.add(Term.HELP_ADMIN_3.get());
            	msg.add(Term.HELP_ADMIN_4.get());
            	msg.add(Term.HELP_ADMIN_5.get());
            	msg.add(Term.HELP_ADMIN_6.get());
            } else
                throw new InsufficientPermissionException();
        } else if(page.equalsIgnoreCase("2")) {
        	msg.add(Term.HELP_TITLE.get("2"));
        	msg.add(Term.HELP_16.get());
        	msg.add(Term.HELP_11.get());
        	msg.add(Term.HELP_12.get());
        	msg.add(Term.HELP_13.get());
        	msg.add(Term.HELP_17.get());
        	msg.add(Term.HELP_18.get());
        	msg.add(Term.HELP_19.get());
        	msg.add(Term.HELP_20.get());
        	msg.add(Term.HELP_21.get());
        	msg.add(Term.HELP_22.get());
        } else {
        	msg.add(Term.HELP_TITLE.get("1"));
        	msg.add(Term.HELP_1.get());
        	msg.add(Term.HELP_2.get());
        	msg.add(Term.HELP_3.get());
        	msg.add(Term.HELP_4.get());
        	msg.add(Term.HELP_5.get());
        	msg.add(Term.HELP_6.get());
        	msg.add(Term.HELP_7.get());
        	msg.add(Term.HELP_25.get());
        	msg.add(Term.HELP_26.get());
        	msg.add(Term.HELP_8.get());
        	msg.add(Term.HELP_9.get());
        	msg.add(Term.HELP_10.get());
        	msg.add(Term.HELP_14.get());
        	msg.add(Term.HELP_15.get());
        	
        	if (scs.hasPermission(cs, Properties.permAdmin))
        		msg.add(Term.HELP_23.get());
        	
        	msg.add(Term.HELP_24.get());

        }
        Messaging.mlSend(player, msg);
        return true;
    }
}
