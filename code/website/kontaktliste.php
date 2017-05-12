<!DOCTYPE html>
<?php
SESSION_START();
$_SESSION["username"] = $_POST["name"];
$users = file("users.txt");
$exists = 1;



for($i=0;$i < count($users); $i++){
if(strcmp(trim($users[$i]), trim($_POST["name"])) == 0){
#$file ="users.txt";
#fwrite($file,$users[$i]." ".$_POST["name"]."\r\n");
#file_put_contents($file, "existiert \r\n", FILE_APPEND);
$exists = 0;
}

}
if(($exists == 1 && $_POST['name'] != "")){
$file ="users.txt";
#fwrite($file, $_POST["name"]."\r\n");
file_put_contents($file,trim($_POST["name"])."\r\n", FILE_APPEND);
#fclose($file);
}
?>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="design.css">
  <title>CNR Group</title>
</head>
<body style="text-alignment:center;">


    <nav>

                <a title="home.html" href="#home">Home</a>
                <a title="Produkte.html" href="home.html#produkte">Produkte</a>
                <a title="Firmenprofil.html" href="home.html#leitung">Über uns</a>
                <a title="Impressum.html" href="Impressum.html">Impressum</a>
                <a title="Registrieren.html" href="home.html#reg">Registrieren</a>
                <a title="chat.php" href="login.php">Zum Chat</a>
        </nav>


<h1 style="padding-top:100px;">CNR Messenger</h1>


<form id="loginform" action="chat.php" method="post">
<input type="submit" name="weiter">
<select name="kontakte">
<option value="" disabled selected hidden>Wähle deinen Chatpartner..</option>
<?php $datei=fopen("users.txt","r+"); 
while(!feof($datei)) 
{ 
$zeile = fgets($datei,1000); 

echo "<option value='".trim($zeile)."'>".trim($zeile)."</option>";
}
fclose($datei);

if(isset($_POST['kontakte'])){
$_SESSION['chatpartner'] = $_POST['kontakte'];
}
?>
</select>


</form>



</body>
</html>
