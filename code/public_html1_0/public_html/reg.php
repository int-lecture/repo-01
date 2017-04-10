<?php

$name = $_POST["name"];
$vorname = $_POST["vorname"];
$email = $_POST["email"];



if (isset($_POST['datenschutz'])){

$file = fopen("testfile.txt", "a") or die ("unable to open file");

fwrite($file, $name . " ");
fwrite($file, $vorname . "");
fwrite($file, $email . "\n");
fclose($file);


 echo"<!DOCTYPE html>
 <head>
<meta http-equiv='refresh' content='3;url=home.html#reg'>
  <meta charset='utf-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <link rel='stylesheet' href='design.css'>
  <title>CNR Group</title>
 </head>

 <body style='text-align:center' 'background-color: grey'>
 <h2><span style='color:black;'>Daten wurden erfolgreich gespeichert</span></h2>
 </body>
 </html>";


}else {
 echo"<!DOCTYPE html>
 <head>
<meta http-equiv='refresh' content='3;url=home.html#reg'>
  <meta charset='utf-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <link rel='stylesheet' href='design.css'>
  <title>CNR Group</title>
 </head>

 <body style='text-align:center' 'background-color: grey'>
 <h2><span style='color:black;'>Bitte stimmen Sie den <br>Datenschutzbestimmungen zu</span></h2>
 </body>
 </html>";


}

	
?>























