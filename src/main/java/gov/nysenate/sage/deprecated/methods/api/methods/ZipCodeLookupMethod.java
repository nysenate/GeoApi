package gov.nysenate.sage.deprecated.methods.api.methods;

import gov.nysenate.sage.model.ApiExecution;


public class ZipCodeLookupMethod extends ApiExecution
{
 /* AddressService addressService;

  public ZipCodeLookupMethod() throws Exception
  {
    addressService = new AddressService();
  } // ZipCodeLookupMethod()


  @Override
  public Response execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiException
  {
    String type = more.get(RequestCodes.TYPE.code());
    if (!type.equals("extended")) {
      throw new ApiTypeException(type);
    }

    Result result = addressService.validate(new Address(
      request.getParameter("addr1"),
      request.getParameter("addr2"),
      request.getParameter("city"),
      request.getParameter("state"),
      "",
      ""
    ), "usps");

    if (result == null) {
      throw new ApiInternalException();
    }
    else if (result.getStatus().equals("0")) {
      return new ValidateResponse(result.getAddress());
    }
    else {
      String msg = "";
      for (String m : result.getMessages()) {
        msg += "\n"+m;
      }
      return new ErrorResponse(msg.toString());
    }
  } // execute() */
}
