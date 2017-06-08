$( document ).ready(function() {

});

function loginUser() {



    var URL = "http://141.19.142.55:5001/login/";
    var jsonObj = {'user': $("#nm2").val(), 'password': $("#pwd2").val() };

                alert(JSON.stringify(jsonObj));

                $.ajax({
            url: URL,
            type: 'POST',
            data: JSON.stringify(jsonObj),
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