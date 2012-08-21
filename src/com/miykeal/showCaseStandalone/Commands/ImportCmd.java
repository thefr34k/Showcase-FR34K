package com.miykeal.showCaseStandalone.Commands;

import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.Storage.FastFileShopStorage;
import com.miykeal.showCaseStandalone.Storage.ShowCaseImport;
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
public class ImportCmd extends GenericCmd {
    
    public ImportCmd(CommandSender cs, String args[]){
        super(cs, args);
        this.mustBePlayer = false;
        this.permission = Properties.permAdmin;
        this.minArg = 2;
    }

	@Override
    public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
        if(errorCheck())
            return true;
                    
        // import from showcase
        if(args[1].equalsIgnoreCase("showcase")){
            ShowCaseStandalone.slog(Level.INFO, "Import Showcase shops.");
            ShowCaseImport si = new ShowCaseImport(scs);
            if(!si.fileExists()){
                Messaging.send(cs, "Could not attach to showcases.csv.  Is it in your ShowCaseStandalone data folder?");
                return true;
            }
            
            
            try {
            	Messaging.send(cs, "Imported "+scs.getShopHandler().importStorage(si)+" shops");
            } catch (IOException ioe) {
            	ShowCaseStandalone.slog(Level.WARNING, "Couldn't import ShowCase shops");
            	Messaging.send(cs, "`rInternal exception: "+ioe);
            }
        }
        
        
        // import from ffss
        else if (args[1].equalsIgnoreCase("ffss")) {
        	if(!(scs.getShopStorage() instanceof FastFileShopStorage)){
                ShowCaseStandalone.slog(Level.INFO, "Import FastFileShopStorage.");
                try {
                    scs.getShopHandler().importStorage(new FastFileShopStorage(scs));
                } catch (IOException ioe) {
                    ShowCaseStandalone.slog(Level.INFO, "IOError: could not import from FastFileShopStorage.");
                    Messaging.send(cs, Term.ERROR_IMPORT.get("FastFileShopStorage"));
                }
            } else {
                Messaging.send(cs, Term.ERROR_USING_ALREADY.get("FastFileShopStorage"));
            }
        }

        //add DB when ready

        else 
            throw new MissingOrIncorrectArgumentException();
        
        return true;
    }
}
