<?php
// A simple script to test the Yahoo YQL and YBOSS Geo APIs
// Adapted from a sample PHP script provided by Yahoo
//
// Project: SAGE
// Author: Ken Zalewski
// Organization: New York State Senate
// Date: 2013-03-14
//
// NOTE: To use this script with API keys (YQL and YBOSS), you will need to
// create a configuration file named yahoo_geo.cfg with the following entries:
//    yql.key=<MY_YQL_CONSUMER_KEY>
//    yql.secret=<MY_YQL_CONSUMER_SECRET>
//    yboss.key=<MY_YBOSS_CONSUMER_KEY>
//    yboss.secret=<MY_YBOSS_CONSUMER_SECRET>
//
// The config file is not necessary when using the YQL public service.
//
// The -f option can be used to specify a different filename for the
// configuration file.


require('OAuth.php');

define('DEFAULT_CONFIG_FILE', 'yahoo_geo.cfg');
define('PUBLIC_BASE_URL', 'http://query.yahooapis.com/v1/public/yql');
define('YQL_BASE_URL', 'http://query.yahooapis.com/v1/yql');
define('YBOSS_BASE_URL', 'http://yboss.yahooapis.com/geo/placefinder');


$prog = $argv[0];
$cfgfile = DEFAULT_CONFIG_FILE;
$use_json = false;
$decode_json = false;
$use_oauth = false;
$use_yboss = false;
$location = '';

if ($argc <= 1) {
  echo "Usage: $prog [-f cfgfile] [--json [--decode]] [--xml] [--oauth] [--yboss] address\n";
  exit(1);
}

for ($i = 1; $i < $argc; $i++) {
  switch ($argv[$i]) {
    case '--config-file': case '-f':
      $cfgfile = $argv[++$i];
      break;
    case '--decode': case '-d':
      $decode_json = true;
      break;
    case '--json': case '-j':
      $use_json = true;
      break;
    case '--oauth': case '-o':
      $use_oauth = true;
      break;
    case '--xml': case '-x':
      $use_json = false;
      break;
    case '--yboss': case '-y':
      $use_yboss = true;
      break;
    default:
      $location = $location.' '.$argv[$i];
  }
}

$args = array();

if ($use_yboss) {
  $args['location'] = $location;
  if ($use_json) {
    $args['flags'] = 'J';
  }
}
else {
  $yql = "select * from geo.placefinder where text='$location'";
  $args['q'] = $yql;
  if ($use_json) { 
    $args['format'] = "json";
  }
}

$base_url = PUBLIC_BASE_URL;
if ($use_yboss) {
  $base_url = YBOSS_BASE_URL;
  $key_keyname = 'yboss.key';
  $secret_keyname = 'yboss.secret';
}
else if ($use_oauth) {
  $base_url = YQL_BASE_URL;
  $key_keyname = 'yql.key';
  $secret_keyname = 'yql.secret';
}

if ($use_oauth) {
  $cfgparms = parse_ini_file($cfgfile);
  if ($cfgparms == false) {
    echo "$prog: $cfgfile: Unable to open/read config file\n";
    exit(1);
  }

  $cc_key = $cfgparms[$key_keyname];
  $cc_secret = $cfgparms[$secret_keyname];

  $consumer = new OAuthConsumer($cc_key, $cc_secret);
  $request = OAuthRequest::from_consumer_and_token($consumer, null, 'GET', $base_url, $args);
  $request->sign_request(new OAuthSignatureMethod_HMAC_SHA1(), $consumer, null);
  $headers = array($request->to_header());
}
else {
  $headers = null; 
}

$url = sprintf("%s?%s", $base_url, OAuthUtil::build_http_query($args));

echo "Sending Curl request [$url]\n";

$ch = curl_init();
$headers and curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
$rsp = curl_exec($ch);
if ($use_json && $decode_json) {
  $rsp = json_decode($rsp);
}
print_r($rsp);
echo "\n";
?>
