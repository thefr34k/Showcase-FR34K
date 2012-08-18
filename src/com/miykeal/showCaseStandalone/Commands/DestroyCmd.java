package com.miykeal.showCaseStandalone.Commands;

import org.bukkit.command.CommandSender;

import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.ShopInternals.Todo;
import com.miykeal.showCaseStandalone.ShopInternals.Todo.Type;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;

public class DestroyCmd extends GenericCmd {
	
	public DestroyCmd (CommandSender cs, String args[]) {
		super (cs, args);
		permission	= Properties.permAdmin;
	}

	@Override
	public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
		if (errorCheck())
			return true;
		
		Messaging.send	(player, next);
		scs.addTodo		(player, new Todo(player, Type.DESTROY, null, 0, null));
		
		return true;
	}

}
