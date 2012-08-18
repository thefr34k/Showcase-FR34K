package com.miykeal.showCaseStandalone.Commands;

import org.bukkit.command.CommandSender;

import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.ShopInternals.Todo;
import com.miykeal.showCaseStandalone.ShopInternals.Todo.Type;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;

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
public class PriceCmd extends GenericCmd {
    
    public PriceCmd(CommandSender cs, String args[]){
        super(cs, args);
        this.permission = Properties.permManage;
        this.minArg = 2;
    }

    @Override
    public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
        
        if(errorCheck())
            return true;
  
        double price = -1;
        try {
            price = Double.parseDouble(args[1]);
        } catch (Exception e) {}
        
        if (price < 0)
            throw new MissingOrIncorrectArgumentException();
        
        Messaging.send(player, next);
        scs.addTodo(player, new Todo (player, Type.SETPRICE, null, price, null));
        
        return true;
    }
}
