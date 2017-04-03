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

echo "Daten erfolgreich gespeichert.";

}else {

echo "Stimmen Sie den Datenschutzbestimmungen zu.";
}

	
?>



