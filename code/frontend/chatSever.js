$(document).ready(function () {
	
	readCookie()
	
	recieveMessages();
	
}





function recieveMessages (){
	//Code für das erhalten der nicht gelesenen Nachrichten
	var URL = 141.19.142.55:5001 +"/messages/" + pseudonym
	
	     $.ajax({
            headers: {
                "Authorization": token
            },
            url: URL,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            dataType: 'json',
            success: function (result, textStatus, xhr) {
                if (xhr.status == 200) {
                    if (typeof sequenceNumber == 'undefined') {
                        messages = result;
                        showMessages();
                    }
                    else {
                        messages = result;
                        sequenceNumber = sequenceNumber + result.length;
                        showMessages();
                    }
                } else if (xhr.status == 204) {

                }
            },
            error: function (xhr, a, b) {
            	alert("Leider ist da etwas schief gelaufen :(\nBeim abrufen Ihrer Nachricht gab es einen Fehler : " + xhr.status + ".\n Loggen Sie sich erneut ein.");
                window.location.href = "loginApplication.html";
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