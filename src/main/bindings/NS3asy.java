package bindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface NS3asy extends Library {
	Library[] dependencies = {Native.load(("ns3.29-applications-debug"), Library.class), 
							  Native.load(("ns3.29-wifi-debug"), Library.class)};
	
    NS3asy INSTANCE = (NS3asy) Native.load(("ns3.29-ns3asy-debug"), NS3asy.class);
    
    /** <i>native declaration : line 19</i> */
	public interface SetOnReceiveFtn_ftn_callback extends Callback {
		void apply(String ip, int port, double time);
	};
	/** <i>native declaration : line 21</i> */
	public interface SetOnPacketReadFtn_ftn_callback extends Callback {
		void apply(String receiverIp, int receiverPort, String senderIp, int senderPort, 
				Pointer payload, int payloadLength, double time);
	};
	/** <i>native declaration : line 23</i> */
	public interface SetOnAcceptFtn_ftn_callback extends Callback {
		void apply(String receiverIp, int receiverPort, String senderIp, int senderPort, double time);
	};
	/** <i>native declaration : line 25</i> */
	public interface SetOnSendFtn_ftn_callback extends Callback {
		void apply(String senderIp, int senderPort, String receiverIp, int receiverPort, 
				Pointer payload, int payloadLength, double time);
	};
	/**
	 * Original signature : <code>void SetNodesCount(unsigned int)</code><br>
	 * <i>native declaration : line 7</i>
	 */
	void SetNodesCount(int nodesCount);
	/**
	 * Original signature : <code>void AddLink(unsigned int, unsigned int)</code><br>
	 * <i>native declaration : line 9</i>
	 */
	void AddLink(int sourceIndex, int destinationIndex);
	/**
	 * Original signature : <code>void setUdp(bool)</code><br>
	 * <i>native declaration : line 11</i>
	 */
	void setUdp(byte isUdp);
	/**
	 * Original signature : <code>bool isUdp()</code><br>
	 * <i>native declaration : line 13</i>
	 */
	boolean isUdp();
	/**
	 * Original signature : <code>int FinalizeSimulationSetup()</code><br>
	 * <i>native declaration : line 11</i>
	 */
	int FinalizeSimulationSetup();
	/**
	 * Original signature : <code>int FinalizeWithWifiPhy()</code><br>
	 * <i>native declaration : line 13</i>
	 */
	int FinalizeWithWifiPhy();
	/**
	 * Original signature : <code>void SchedulePacketsSending(unsigned int, unsigned int, const char*, int)</code><br>
	 * <i>native declaration : line 13</i>
	 */
	void SchedulePacketsSending(int senderIndex, int nPackets, Pointer payload, int length);
	/**
	 * Original signature : <code>void ResumeSimulation(double)</code><br>
	 * <i>native declaration : line 15</i>
	 */
	void ResumeSimulation(double delay);
	/**
	 * Original signature : <code>void StopSimulation()</code><br>
	 * <i>native declaration : line 17</i>
	 */
	void StopSimulation();
	/**
	 * Original signature : <code>int getNodesCount()</code><br>
	 * <i>native declaration : line 21</i>
	 */
	int getNodesCount();
	/**
	 * Original signature : <code>int getReceiversN(unsigned int)</code><br>
	 * <i>native declaration : line 23</i>
	 */
	int getReceiversN(int sender);
	/**
	 * Original signature : <code>int getReceiverAt(unsigned int, unsigned int)</code><br>
	 * <i>native declaration : line 25</i>
	 */
	int getReceiverAt(int sender, int receiverIndex);
	/**
	 * Original signature : <code>char* getIpAddressFromIndex(unsigned int)</code><br>
	 * <i>native declaration : line 28</i>
	 */
	Pointer getIpAddressFromIndex(int index);
	/**
	 * Original signature : <code>int getIndexFromIpAddress(const char*)</code><br>
	 * <i>native declaration : line 30</i>
	 */
	int getIndexFromIpAddress(String ip);
	/**
	 * Original signature : <code>void SetOnReceiveFtn(SetOnReceiveFtn_ftn_callback*)</code><br>
	 * <i>native declaration : line 19</i>
	 */
	void SetOnReceiveFtn(NS3asy.SetOnReceiveFtn_ftn_callback ftn);
	/**
	 * Original signature : <code>void SetOnPacketReadFtn(SetOnPacketReadFtn_ftn_callback*)</code><br>
	 * <i>native declaration : line 21</i>
	 */
	void SetOnPacketReadFtn(NS3asy.SetOnPacketReadFtn_ftn_callback ftn);
	/**
	 * Original signature : <code>void SetOnAcceptFtn(SetOnAcceptFtn_ftn_callback*)</code><br>
	 * <i>native declaration : line 23</i>
	 */
	void SetOnAcceptFtn(NS3asy.SetOnAcceptFtn_ftn_callback ftn);
	/**
	 * Original signature : <code>void SetOnSendFtn(SetOnSendFtn_ftn_callback*)</code><br>
	 * <i>native declaration : line 25</i>
	 */
	void SetOnSendFtn(NS3asy.SetOnSendFtn_ftn_callback ftn);
}
