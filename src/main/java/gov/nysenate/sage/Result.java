package gov.nysenate.sage;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Graylin Kim
 * @author Ken Zalewski
 *
 * A generic Result structure passed back from all services.
 *
 * Each service translates the 3rd party response into a response object which
 * can be uniformly handled and provides certain consistencies:
 *
 *      1. If status_code = "0" the request was successful
 *      2. If status_code is non-zero then messages propagate any 3rd party response messages.
 *      3. source is the fully encoded url associated with the request
 *      4. Services will either fill addresses or address . See service docs for details.
 *
 * This result structure allows a service which does bulk operations to consistently
 * return an array of results even in the face of partial failure while retaining all
 * relevant error details for further action by the caller.
 */

public class Result
{
  private Address m_address;
  private ArrayList<Address> m_addresses;
  private ArrayList<String> m_messages;
  private String m_status;
  private String m_source;


  public Result(String status, String src)
  {
    m_status = status;
    m_source = src;
    m_address = null;
    m_addresses = new ArrayList<Address>();
    m_messages = new ArrayList<String>();
  } // Result()


  public Result(String src)
  {
    this("0", src);
  } // Result()


  public Result()
  {
    this("0", "");
  } // Result()


  public Address getAddress()
  {
    return m_address;
  }


  public void setAddress(Address addr)
  {
    m_address = addr;
  } // setAddress()


  public List<Address> getAddresses()
  {
    return m_addresses;
  }


  public Address getFirstAddress()
  {
    return m_addresses.get(0);
  }


  public void setAddresses(ArrayList<Address> addrs)
  {
    m_addresses = addrs;
  } // setAddresses()


  public void addAddress(Address addr)
  {
    m_addresses.add(addr);
  } // addAddress()


  public List<String> getMessages()
  {
    return m_messages;
  } // getMessages()


  public String getFirstMessage()
  {
    return m_messages.get(0);
  }


  public void setMessages(ArrayList<String> msgs)
  {
    m_messages = msgs;
  } // setMessages()


  public void addMessage(String msg)
  {
    m_messages.add(msg);
  } // addMessage()


  public String getStatus()
  {
    return m_status;
  } // getStatus()


  public void setStatus(String status)
  {
    m_status = status;
  } // setStatus()


  public String getSource()
  {
    return m_source;
  } // getSource()


  public void setSource(String src)
  {
    m_source = src;
  } // setSource()
}
