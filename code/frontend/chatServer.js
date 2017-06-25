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
	var message = document.getElementById("message").value;
	var myJSON = {
		"token":getToken(),
		"from":readCookie("pseudonym"),
		"date":getDate(),
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
            printMessage(readCookie("pseudonym"), message, getDate());

		},
		error : function(xhr,status,error) {
			alert("fehler");
		}
	});	
}

function printMessage(name, message, date) {
    $("#chatbody").append(date+" " + name + ": " + message);
    document.getElementById("message").value = "";

}

function getDate() {
    var d = new Date();
	var stringDate = d.getFullYear() + "-" + ((d.getMonth() + 1) < 10 ? "0" + (d.getMonth() + 1) : (d.getMonth() + 1)) + "-" + ((d.getDate()) < 10 ? "0" + (d.getDate()) : (d.getDate())) + "T" + (d.getHours() < 10 ? "0" + d.getHours() : d.getHours()) + ":" + (d.getMinutes() < 10 ? "0" + d.getMinutes() : d.getMinutes()) + ":" + ((d.getSeconds() < 10 ? "0" + d.getSeconds() : d.getSeconds())) + "+0200";
    return stringDate;
}