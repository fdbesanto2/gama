/*********************************************************************************************
 *
 * 'IConnector.java, in plugin gama.extensions.network, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package gama.extensions.network.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gama.common.interfaces.IAgent;
import gama.extensions.messaging.GamaMessage;
import gama.runtime.scope.IScope;

public interface IConnector {
	void connect(IAgent agent) throws GamaNetworkException;

	void close(final IScope scope) throws GamaNetworkException;

	void send(IAgent agent, String dest, GamaMessage data);

	List<ConnectorMessage> fetchMessageBox(final IAgent agt);

	void configure(String parameterName, String value);

	void joinAGroup(final IAgent agt, final String groupName);

	void leaveTheGroup(final IAgent agt, final String groupName);

	Map<IAgent, LinkedList<ConnectorMessage>> fetchAllMessages();

	void forceNetworkUse(boolean b);

	String SERVER_URL = "SERVER_URL";
	String SERVER_PORT = "SERVER_PORT";
	String LOCAL_NAME = "LOCAL_NAME";
	String LOGIN = "LOGIN";
	String PASSWORD = "PASSWORD";
	String PACKET_SIZE = "PACKET_SIZE";

}
