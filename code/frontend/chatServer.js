$(document).ready(function () {
	readCookie()
		
});



var sequenceNumber = 0;



function recieveMessages (){
	//Code für das erhalten der nicht gelesenen Nachrichten
	readCookie();	
	var URL = "http://141.19.142.55:5000/messages/" + pseudonym + "/" + sequenceNumber;
		
	
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
					$.each(result,function(index, value){
						 if (value.sequence != sequenceNumber) {
            
      
					// alert('My array has at position ' + index + ', this value: ' + value.to);
					$("ol").append("<li style='background-color:red;'>"+"erhalten von " +value.from+": "+value.text+"</li>");
						 }
					});
					sequenceNumber = result[result.length -1].sequence;
					
					
            },
			complete : function(result){
			
			
					
					
			
		},
            error: function (xhr, a, b) {
            	alert("Leider ist da etwas schief gelaufen :(\nBeim abrufen Ihrer Nachricht gab es einen Fehler : " + xhr.status + ".\n Loggen Sie sich erneut ein.");
                
		alert(token);
                //alert("getMessages von " + pseudonym + " fehlgeschlagen");
            }

        });
	setTimeout(recieveMessages,1000)
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
	readCookie();
	var chatPartner = $("#csearch").val();
	var message = $("#message").val();
	var myJSON = {
		"token":token,
		"from":pseudonym,
		"date":getMyDate(),
		"to":chatPartner,
		"text":message
	};

	$.ajax({
		url: "http://141.19.142.55:5000/send",
		type: "PUT",
		contentType: "application/json; charset=utf-8",
		dataType:"json",
		async:false,
		data: JSON.stringify(myJSON),
		complete : function(response){
			$("ol").append("<li style='background-color:lightgreen;'>"+getMyDate()+": "+message+"</li>");
			
		},
		error : function(xhr,status,error) {
			
		}
	});	
}


function getMyDate() {
    var date = new Date();
    var stringDate = date.getFullYear() + "-" + ((date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1)) + "-" + ((date.getDate()) < 10 ? "0" + (date.getDate()) : (date.getDate())) + "T" + (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + ":" + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) + ":" + ((date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds())) + "+0200";
    return stringDate;
}


