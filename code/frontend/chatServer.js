$(document).ready(function () {
	
	readCookie()
	});


function getToken(){
	var token = readCookie("token");
	if(!token){
		console.log("bitte melde dich an");
	}
	return token;
}


function recieveMessages (){
	//Code für das erhalten der nicht gelesenen Nachrichten
	var URL = getChatIP() + "/messages/" + pseudonym;
	
	     $.ajax({
            headers: {
                "Authorization": token
            },
            url: URL,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            dataType: 'json',
            success: function (result, textStatus, xhr) {
					//code
					alert(result.from);
					alert(result.to);
            },
            error: function (xhr, a, b) {
            	alert("Leider ist da etwas schief gelaufen :(\nBeim abrufen Ihrer Nachricht gab es einen Fehler : " + xhr.status + ".\n Loggen Sie sich erneut ein.");
                
                //alert("getMessages von " + pseudonym + " fehlgeschlagen");
            }

        });
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

function sendMessage() {
	var message = $("#message").val();
	var myJSON = {
		"token":getToken(),
		"from":readCookie("pseudonym"),
		"date":"2017-06-19T12:36:30+0200",
		"to":document.getElementById("csearch").value,
		"text":message
	};

	$.ajax({
		url: chatIP + "/send",
		type: "PUT",
		contentType: "application/json; charset=utf-8",
		dataType:"json",
		data: JSON.stringify(myJSON),
		succes : function(response){
			alert(message);
		},
		error : function(xhr,status,error) {
			alert("fehler");
		}
	});	
}