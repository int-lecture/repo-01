function hideStuff(id) {
    document.getElementById(id).style.display = 'none';
}

function showStuff(id) {
    document.getElementById(id).style.display = 'block';
}


function loginBtn() {
	
    hideStuff('imgdiv');
    showStuff('sidebar');
    showStuff('menu');
    showStuff('navtext');
    showStuff('input');
    showStuff('chatbox');
    hideStuff('lgin');
    hideStuff('sgnup');
    showStuff('lgout')
}

function chattingWith() {
    document.getElementById("chattingwith").innerHTML = "You are chatting with " + document.getElementById("csearch").value;
	recieveMessages();
}

$("#message").keyup(function(event){
if(event.keyCode == 13){
	$("#btn-chat").click();
}
});
