<%@ page import="gov.nysenate.sage.config.Environment" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%
    Environment env = new Environment();
    String googleMapsUrl = env.getGoogleMapsUrl();
    String googleMapsKey = env.getGoogleMapsKey();

    if (googleMapsKey != null && !googleMapsKey.equals("")) {
        googleMapsUrl = googleMapsUrl + "&key=" + googleMapsKey;
    }
    request.setAttribute("googleMapsUrl", googleMapsUrl);
%>

<fmt:setLocale value = "es_ES"/>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage</jsp:attribute>
    <jsp:attribute name="title">SAGE Map Viewer</jsp:attribute>

    <jsp:attribute name="jsIncludes">
        <!-- Custom Map Styles -->
        <script>
            <c:if test="${customMapStyle == true}">
                var customMapStyle = {
                    "stylers" : [
                        { "hue" : "${hue}" },
                        { "saturation" : ${saturation}},
                        { "lightness" : ${lightness}}
                    ]
                };
            </c:if>

            <c:if test="${customPolyStyle == true}">
                var customPolyStyle = {
                    "hue": "${polyHue}"
                };
            </c:if>
        </script>

        <script type="text/javascript" src="${googleMapsUrl}"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/blockui.js"></script>
        <sage:sage></sage:sage>
        <sage:common></sage:common>
    </jsp:attribute>
    <jsp:body>
        <div id="contentwrapper">
            <div id="mapcontentcolumn">
                <div ng-controller="EmbeddedMapController">
                    <script>
                        districtType = "${districtType}";
                        districtCode = "${districtCode}";
                        width = ${width};
                        height = ${height};
                    </script>
                </div>
                <div id="mapView" ng-controller="EmbeddedMapViewController">
                    <div class="info-container" ng-show="showPrompt" style="width:280px;padding:3px 10px;position:absolute;left:40px;z-index:10000;">
                        <table style="width:100%">
                            <tr>
                                <td>
                                    <a ng-hide="showInfo" ng-click="showInfo=true;">Senator Information</a>
                                    <a ng-show="showInfo" ng-click="showInfo=false;">Senator Information</a>
                                </td>
                                <td class="right-icon-placeholder">
                                    <a ng-hide="showInfo" ng-click="showInfo=true;"><div class="icon-arrow-down4"></div></a>
                                    <a ng-show="showInfo" ng-click="showInfo=false;"><div class="icon-arrow-up4"></div></a>
                                </td>
                            </tr>
                        </table>
                        <div ng-show="showInfo" id="senator-view" style="padding-top:10px;border-top:1px solid #ddd">
                            <div class="mini-senator-pic-holder">
                                <a ng-href="{{senator.url}}" target="_top"><img ng-src="{{senator.imageUrl | senatorPic}}" class="senator-pic" /></a>
                            </div>
                            <div>
                                <p class="senator member-name">
                                    <a target="_blank" ng-href="{{senator.url}}">{{senator.name}}</a>
                                </p>
                                <p class="senate district">Senate District {{district}}</p>
                            </div>
                        </div>
                    </div>
                    <div id="map_canvas"></div>
                </div>
            </div>
        </div>

        <!-- Map Tooltip -->
        <div id="mapTooltip"></div>

        <script>
            $(window).load(function(){
                if (width > 0 && height > 0) {
                    $("body").width(width).height(height);
                }
            });
        </script>
    </jsp:body>
</sage:wrapper>
