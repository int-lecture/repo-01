
$( document ).ready(function() {  
	
});

$('#signup').bind('click', function() {

	var password= $("#pwd").val();
	var myJSON = {
			"pseudonym": $('#nm').val(),
			"user": $('#usr').val(),
			"password": password
		     };
	
    alert(JSON.stringify(myJSON));

	var URL = "http://"+ getRegisterIP()+"/register";

    $.ajax({
		url: URL,
		method: "PUT",
//		type:   "PUT",
		data: JSON.stringify(myJSON),
		async: false,
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success: function(response) {
			alert("Registrierung erfolgreich!");
			//zum Login Fenster wechseln
		},
		error: function(xhr, status, error){
			alert("status " + xhr.status);             
			alert("error " +error); 
			alert("Registrierung fehlgeschlagen");
		}
	});
	return false;

});


/*
$( document ).ready(function() {  
	
});
var ip="141.19.142.55";
function registerUser(){
	var pw1= $("#pwd").val();
	
	
	var URL = "http://"+ip+":5002/register";
	var dataObject = {'pseudonym': $("#nm").val(), 'user': $("#usr").val(), 'password': pw1 };

        alert(JSON.stringify(dataObject));

        $.ajax({
			url: URL,
			type: 'PUT',    
			data: JSON.stringify(dataObject),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: function(result) {
				alert("success?");
				window.location.href = "loginApplication.html";
			},
			error: function(xhr, ajaxOptions, thrownError){
			alert("Error!§§!");
		}
  	});
	return false;

}


*/
