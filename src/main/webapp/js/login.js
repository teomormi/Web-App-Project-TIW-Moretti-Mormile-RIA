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
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('user', message);
                window.location.href = "home.html";
                break;
              case 400: // bad request
                document.getElementById("errormessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errormessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errormessage").textContent = message;
                break;
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
    if (form.checkValidity()) {
      makeCall("POST", 'CreateUser', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('user', message);
                window.location.href = "home.html";
                break;
              case 400: // bad request
                document.getElementById("errorsignup").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorsignup").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorsignup").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();