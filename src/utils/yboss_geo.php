<?php
// A simple script to test the Yahoo BOSS Geo API
// Adapted from a sample PHP script provided by Yahoo
//
// Project: SAGE
// Author: Ken Zalewski
// Organization: New York State Senate
// Date: 2012-12-17
//
// NOTE: To use this script, you will need to create a configuration file
// called yboss_geo.cfg with two entries:
//    key=<MY_CONSUMER_KEY>
//    secret=<MY_CONSUMER_SECRET>
//
// The -f option can be used to specify a different filename for the
// configuration file.


require('OAuth.php');

define('DEFAULT_CONFIG_FILE', 'yboss_geo.cfg');
define('BASE_URI', 'http://yboss.yahooapis.com/geo/placefinder');

$prog = $argv[0];
$cfgfile = DEFAULT_CONFIG_FILE;
$use_json = false;
$location = '';

if ($argc <= 1) {
  echo "Usage: $prog [-f cfgfile] [--json] [--xml] address\n";
  exit(1);
}

for ($i = 1; $i < $argc; $i++) {
  switch ($argv[$i]) {
    case '-f':
      $cfgfile = $argv[++$i];
      break;
    case "--json":
      $use_json = true;
      break;
    case "--xml":
      $use_json = false;
      break;
    default:
      $location = $location.' '.$argv[$i];
  }
}

$cfgparms = parse_ini_file($cfgfile);
if ($cfgparms == false) {
  echo "$prog: $cfgfile: Unable to open/read config file\n";
  exit(1);
}

$cc_key = $cfgparms['key'];
$cc_secret = $cfgparms['secret'];

$args = array();
$args['location'] = $location;
if ($use_json) { 
  $args['flags'] = "J";
}

$consumer = new OAuthConsumer($cc_key, $cc_secret);
$request = OAuthRequest::from_consumer_and_token($consumer, NULL, "GET", BASE_URI, $args);
$request->sign_request(new OAuthSignatureMethod_HMAC_SHA1(), $consumer, NULL);

$url = sprintf("%s?%s", BASE_URI, OAuthUtil::build_http_query($args));

$ch = curl_init();
$headers = array($request->to_header());
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
$rsp = curl_exec($ch);
if ($use_json) {
  $rsp = json_decode($rsp);
}
print_r($rsp);
?>
