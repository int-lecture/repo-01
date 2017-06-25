/*

$( document ).ready(function() {  
	
});

$('#loginbtn').bind('click', function() {

 var ip="141.19.142.55";
	var URL = "http://"+ip+":5001/login/";
	var dataObject = {'user': $("#nm2").val(), 'password': $("pwd2#").val()};

        alert(JSON.stringify(dataObject));

        $.ajax({
			url: URL,
			type: 'POST',    
			data: JSON.stringify(dataObject),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: function(result) {
				document.cookie = "token="+result.token;
				document.cookie="pseudonym="+result.pseudonym+";expires="+result["expire-date"];
				alert("success?");
				window.location.href = "chatApplication.html";
			},
			error: function(xhr, ajaxOptions, thrownError){
				alert(" error");
			}
  	});
	return false;



});

*/
$( document ).ready(function() {  
readCookie();
});


function loginUser() {
	var URL = "http://"+getLoginIP()+"/login";
	var dataObject = {'user': $("#nm2").val(), 'password': $("#pwd2").val()};
	
        alert(JSON.stringify(dataObject));

        $.ajax({
			url: URL,
			method: 'POST',    
			data: JSON.stringify(dataObject),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: function(result) {
				document.cookie = "token="+result.token;
				document.cookie="pseudonym="+result.pseudonym+";expires="+result["expire-date"];
				document.getElementById("navtext").innerHTML = "Logged in as "+pseudonym;
				alert("success?");
				alert(document.cookie);
				alert(pseudonym);
				loginBtn();	
//				window.location.href = "SecureMessenger.html";
			},
			error: function(xhr, ajaxOptions, thrownError){
				alert(xhr.Status +" error");
			}
  	});
	return false;

}


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
    showStuff('lgout');
    hideStuff('sModal');
}

function readCookie() {
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    $.each(ca, function (index, value) {
        value = value.trim();
        if (value.substring(0, "token=".length) == "token=") {
            token = value.substring(6);
        }
        if (value.substring(0, "pseudonym=".length) == "pseudonym=") {
            pseudonym = value.substring("pseudonym=".length);
        }
    });
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

document.getElementById("navtext").innerHTML="You are logged in as " + getCookie("pseudonym");


