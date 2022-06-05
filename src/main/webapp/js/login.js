/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("buttonlogin").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            if (x.status == 200) {
				sessionStorage.setItem('user', message);
                window.location.href = "home.html";
             } else {
					document.getElementById("errormessage").textContent = message;
			 }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });
  
  document.getElementById("buttonsign").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    var pass1 = form.querySelector("input[name='password']").value;
	var pass2 = form.querySelector("input[name='passconfirm']").value;
	if(pass1 != pass2) {
		document.getElementById("errorsignup").textContent = "The two passwords must be identical";
		return;
	}
    var email = form.querySelector("input[name='email']").value;  
    var validRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;    
	if(!email.match(validRegex)){
		document.getElementById("errorsignup").textContent = "Please insert a valid email";
		return;
	}  
   
    if (form.checkValidity()) {
      makeCall("POST", 'CreateUser', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            if (x.status == 200) {
				sessionStorage.setItem('user', message);
                window.location.href = "home.html";
             } else {
					document.getElementById("errorsignup").textContent = message;
			 }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();