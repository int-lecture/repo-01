
<!DOCTYPTE html>

<link rel="stylesheet" href="design.css">


<?php 
    # Session starten nicht vergessen !!! Sonst gibts keine Daten ! 
    SESSION_START(); 

$chatpartner = $_POST["kontakte"];
$name = $_SESSION["username"];
$testcp = $_SESSION["chatpartner"];
if(isset($_POST['kontakte'])){
$_SESSION["chatp"] = $_POST["kontakte"];
}
$cp = $_SESSION["chatp"];
$deccp = crc32(trim($cp));
$decname = crc32(trim($name));
$chatfile = $decname + $deccp;
?>


<body style="text-align:center;">
      <nav>

                <a title="home.html" href="home.html#home">Home</a>
                <a title="Produkte.html" href="home.html#produkte">Produkte</a>
                <a title="Firmenprofil.html" href="home.html#leitung">Über uns</a>
                <a title="Impressum.html" href="Impressum.html">Impressum</a>
                <a title="Registrieren.html" href="home.html#reg">Registrieren</a>
                <a title="chat.php" href="login.php">Zum Chat</a>
        </nav>

<div class="chatwriting" style="padding-bottom:100x;">
<h1> CNR Messenger</h1>

<form id="loginform" method="post" style="padding-top:0px;" >

<?php
echo'<div class="infotext">';
echo"Username: ".$name."<br> Chat to: ".$cp;
echo '</div>';
$text = $_POST["text"];

# Hier wird der Text welcher über das Textfeld eingegeben wird
# in das textfile im Ordner chats geschrieben. Falls dieses noch nicht
# exisitiert wird es erstellt. Der name ergibt sich aus beiden Chatpartnern


#if(strcmp($text,"")!==0){
$handle = fopen ("./chats/".$chatfile, a);
fwrite ($handle, $_SESSION['username'].": ".$text."\r\n");
fclose ($handle);
#}
echo '</div>';

# Hier wird der aktuelle chat geladen und ausgegeben

echo'<div style=" border:1px;
 border-style:solid;
 padding:12px 20px;
 min-height:250px;
 max-height:250px;
 width:900px;
 text-align:left;
 overflow:scroll;
 margin-left:23%">';

$_SESSION['chatpartner'] = $cp;
$datei=fopen("./chats/".$chatfile,"r+"); 
while(!feof($datei)) 
{ 
$zeile = fgets($datei,1000); 
echo $zeile."<br>"; 
} 
fclose($datei);  

echo '</div>';

?>
<div id="chatinput">
<input type="text" name="text" placeholder="Schreibe deinen Text...">
<br>
<input type="submit" name="senden">
<input type="submit" name="refresh" value="Refresh">

</div>
</form>


</body>
</html>
