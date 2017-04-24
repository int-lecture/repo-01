<html>
<body>
<form action="chat.php" method="post">
Username: <input type="text" name="name"><br>
Chatpartner: <select name="chatpartner">
<?php
$file = fopen("users.txt", "r") or die("Unable to open file!");
$users = explode("\n", fread($file, filesize("users.txt")));
for($i=0; $x<count($users);i++){
echo '<option value="' + $i + '">' + $users[i] + '</option>';
}
?>
<input type="submit">
</form>
</body>
</html>
