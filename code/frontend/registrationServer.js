$( document ).ready(function() {
	
});

var regIP = getRegisterIP();

function registerUser() {


	
	var URL = regIP + "/register/";
	var jeisohnObj = {'pseudonym': $("#nm").val(), 'user': $("#usr").val(), 'password': $("#pwd").val() };

        alert(JSON.stringify(jeisohnObj));

        $.ajax({
			url: URL,
			type: 'PUT',    
			data: JSON.stringify(jeisohnObj),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: function(result) {
				alert("success?");
				window.location.href = "SecureMessenger.html";
			},
			error: function(xhr, ajaxOptions, thrownError){
			alert("Fehler!");
		}
  	});
	return false;
}