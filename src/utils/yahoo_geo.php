<?php
// A simple script to test the Yahoo YQL and YBOSS Geo APIs
// Adapted from a sample PHP script provided by Yahoo
//
// Project: SAGE
// Author: Ken Zalewski
// Organization: New York State Senate
// Date: 2013-03-14
// Revised: 2013-04-09
//
// This script allows the user to test the three different methods of making
// geocoding requests using Yahoo's APIs:  YQL public, YQL keyed, and YBOSS.
//
// To use this script with API keys (YQL keyed and YBOSS), you will need to
// specify the --oauth command line option.  In addition, you will need to
// create at least one config file with the following entries:
//    key=<MY_CONSUMER_KEY>
//    secret=<MY_CONSUMER_SECRET>
//
// By default, the script will look for a config file named yahoo_geo.cfg
// whenever the --oauth option is specified.  Use the -f option to specify
// a different filename for the config file.
//
// The config file is not necessary when using the YQL public service, since
// API keys are not required to access it.
//

require('OAuth.php');

define('DEFAULT_CONFIG_FILE', 'yahoo_geo.cfg');
define('PUBLIC_BASE_URL', 'http://query.yahooapis.com/v1/public/yql');
define('YQL_BASE_URL', 'http://query.yahooapis.com/v1/yql');
define('YBOSS_BASE_URL', 'http://yboss.yahooapis.com/geo/placefinder');


$prog = $argv[0];
$usage = "Usage: $prog [-f cfgfile] [--json [--decode]] [--xml] [--oauth] [--yboss] address [address ...]";
$cfgfile = DEFAULT_CONFIG_FILE;
$use_json = false;
$decode_json = false;
$use_oauth = false;
$use_yboss = false;
$locations = array();

if ($argc <= 1) {
  echo "$usage\n";
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
    case '--help': case '-h':
      echo "$usage\n";
      exit(0);
      break;
    default:
      if ($argv[$i][0] == '-') {
        echo "$prog: {$argv[$i]}: Invalid option\n";
        exit(1);
      }
      else {
        $locations[] = $argv[$i];
      }
  }
}

if (count($locations) < 1) {
  echo "$prog: Must specify at least one address\n$usage\n";
  exit(1);
}

$args = array();

if ($use_yboss) {
  if (count($locations) > 1) {
    echo "$prog: Warning: YBOSS can handle only one address per request; using first address specified\n";
  }
  $args['location'] = $locations[0];
  if ($use_json) {
    $args['flags'] = 'J';
  }
}
else {
  $yql = "select * from geo.placefinder where text='{$locations[0]}'";
  for ($i = 1; $i < count($locations); $i++) {
    $yql .= " or text='{$locations[$i]}'";
  }
  $args['q'] = $yql.';';
  if ($use_json) { 
    $args['format'] = "json";
  }
}

$base_url = PUBLIC_BASE_URL;
if ($use_yboss) {
  $base_url = YBOSS_BASE_URL;
}
else if ($use_oauth) {
  $base_url = YQL_BASE_URL;
}

if ($use_oauth) {
  $cfgparms = parse_ini_file($cfgfile);
  if ($cfgparms == false) {
    echo "$prog: $cfgfile: Unable to open/read config file\n";
    exit(1);
  }

  if (isset($cfgparms['key']) && isset($cfgparms['secret'])) {
    $cc_key = $cfgparms['key'];
    $cc_secret = $cfgparms['secret'];
  }
  else {
    echo "$prog: $cfgfile: Must contain both key= and secret= parameters\n";
    exit(1);
  }

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
