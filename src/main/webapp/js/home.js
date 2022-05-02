{ // avoid variables ending up in the global scope
	  // page components
		let headerName, albumsList, imageDetails, imagesList, commentCreator,pageOrchestrator = new PageOrchestrator();


	  window.addEventListener("load", () => {
	    if (sessionStorage.getItem("user") == null) {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();  // display initial content
	    }
	  }, false);

		function PageOrchestrator() {
	    var alertContainer = document.getElementById("alert"); // re use alertContainer as param to obj that manage alert

	    this.start = function() {
	      headerName = new HeaderName(sessionStorage.getItem('user'),document.getElementById("user_name"));
	      headerName.show();

	      albumsList = new AlbumsList(
	        alertContainer,
	        document.getElementById("albums_container"),
	        document.getElementById("albums_containerbody"));
		  albumsList.registerEvents(document.getElementById("save_list"));

		  imagesList = new ImagesList(
					alertContainer,
	        document.getElementById("album_images"));
	      //imagesList.registerEvents(document.getElementById("button_left"),document.getElementById("button_right"))

	      imageDetails = new ImageDetails(
					alertContainer,
	        document.getElementById("image_description"),
	        document.getElementById("comment_container"));
	       // imagesList.registerEvents(document.getElementById("button_comment")))

	     	// add UploadImage e CreateAlbum obj
			// need the orchestrator when refresh al pages
	 
	      document.getElementById("button_logout").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })
	    };

	    this.refresh = function(currentAlbum) {
	      alertContainer.textContent = "";
	      albumsList.reset();
		  imagesList.reset();
	      imageDetails.reset();

		  albumsList.show(function() {
		 		albumsList.autoclick(currentAlbum);
		  });
	    };
	  }

		function HeaderName(_username, _namecontainer) {
	 	 this.username = _username;
		 this.namecontainer = _namecontainer;

	 	 this.show = function() {
			 this.namecontainer.textContent = this.username;
	 	 }
	  }

		function AlbumsList(_alert, _albums_container, _albums_containerbody) { // add album name
			this.alert = _alert;
			this.albums_container = _albums_container;
			this.albums_containerbody = _albums_containerbody;
			this.saveBtn;
			this.startElement;

			this.reset = function() {
				this.albums_container.style.visibility = "hidden";
				this.startElement  = null;
			}

			this.registerEvents = (_button) => {
				this.saveBtn = _button;
				this.saveBtn.style.display = "none";
				this.saveBtn.addEventListener("click", (e) => {
					var self = this;
					var input = this.getAlbumsOrder(); 
					var param = 'objarray=' + input;
					 $.ajax({
      					url: 'SaveAlbumsOrder',
      					type: 'POST', 
      					dataType: 'json',  
      					data: param,
      					success:    function(data){},
   						error:      function(xhr, textStatus, error){
      						 self.alert.textContent = error;
   						}
    				});
					this.saveBtn.style.display = "none";
				});
			}

			this.show = function(next) {
				var self = this;
				makeCall("GET", "GetAlbumsList", null, // restituisce oggetto con due array
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								var albumsToShow = JSON.parse(req.responseText);
								self.update(albumsToShow); // call method update of AlbumsList obj
								if (next) next(); // show specific album
						} else {
							self.alert.textContent = message;
						}
					}
				});
			};

			this.update = function(arraysAlbums) {
				console.log(arraysAlbums[0]);
				console.log(arraysAlbums[1]);
				
				this.albums_containerbody.innerHTML = ""; // empty the table body
				var self = this;

				
				this.albums_containerbody.appendChild(this.createTitle("OTHER ALBUMS"));

				arraysAlbums[0].forEach(function(album) { // show other albums
					row = self.createItem(album);
					self.albums_containerbody.appendChild(row);
				});

				this.albums_containerbody.appendChild(this.createTitle("YOUR ALBUMS"));
				
				arraysAlbums[1].forEach(function(album) { // show your albums
						row = self.createItem(album);
						row.draggable = true;
						row.classList.add('draggable')
	        			row.addEventListener("dragstart", self.dragStart); //save dragged element reference
	        			row.addEventListener("dragover", self.dragOver); // change color of reference element to red
	        			row.addEventListener("dragleave", self.dragLeave); // change color of reference element to black
	        			row.addEventListener("drop", self.drop); //change position of dragged element using the referenced element
						self.albums_containerbody.appendChild(row);
					});
				
				this.albums_containerbody.style.visibility = "visible";
			}
			
			this.createTitle = function (message){
				var rowtitle, tdtitle, title,text;
				rowtitle = document.createElement("tr");
				tdtitle = document.createElement("td");
				title = document.createElement("h2");
				text = document.createTextNode(message);
				title.appendChild(text);
				tdtitle.appendChild(title);
				rowtitle.appendChild(tdtitle);
				return rowtitle;
			}
			 
			this.createItem = function (albumitem) {
				var rowitem,tditem,text;
				rowitem = document.createElement("tr");
				tditem = document.createElement("td");
				text = document.createTextNode(albumitem.title);
				tditem.appendChild(text);
				rowitem.setAttribute('albumid', albumitem.id); // set a custom HTML attribute
				rowitem.addEventListener("click", () => imagesList.show(albumitem.id));
				rowitem.appendChild(tditem);
				return rowitem;
			}

			this.dragStart = (e) => {
				this.startElement = e.target.closest("tr");
			}

			this.dragOver = (e) => {
	        	e.preventDefault();
	        	var dest = e.target.closest("tr");
	        	dest.classList.add('selected');
	    	}

			this.dragLeave = (e) => {
				var dest = e.target.closest("tr");
				dest.classList.remove('selected');
			}

			this.drop = (e) => {
	        	var dest = e.target.closest("tr");
	        	var table = dest.closest('table');
	        	var rowsArray = Array.from(table.querySelectorAll('tr.draggable'));
	        	var indexDest = rowsArray.indexOf(dest)
				if(rowsArray.indexOf(this.startElement) != indexDest){
					if (rowsArray.indexOf(this.startElement) < indexDest)
	            		this.startElement.parentElement.insertBefore(this.startElement, rowsArray[indexDest + 1]);
	        		else if(rowsArray.indexOf(this.startElement) > indexDest)
	            		this.startElement.parentElement.insertBefore(this.startElement, rowsArray[indexDest]);
	            	this.saveBtn.style.display = "block"; // show save button
				}
				this.unselectRows(rowsArray);
	    	}

			this.unselectRows = function (rowsArray) {
		        for (var i = 0; i < rowsArray.length; i++) {
		            rowsArray[i].classList.remove('selected');
		        }
		  	}

		  	this.getAlbumsOrder = () => {
				var arraytr = Array.from(this.albums_containerbody.querySelectorAll("tr.draggable"));
				return arraytr.map(a => a.getAttribute("albumid"));
			}

			this.autoclick = function(albumId) { // simulate the click on album
				var e = new Event("click");
				var selector = "tr[albumid='" + albumId + "']";
				var rowToClick =  // the first album or the album with id = albumId
					(albumId) ? document.querySelector(selector) : this.albums_containerbody.querySelectorAll("tr")[1];
				if (rowToClick) rowToClick.dispatchEvent(e);
			}
		}


		function ImagesList(_alert, _container) { // add function to navigate beetween images list
			this.alert = _alert;
			this.container = _container;
			this.leftButton;
			this.rightButton;
			this.imagesList; // the complete list of images
			this.actualIndex;

			this.reset = function() {
				this.container.style.visibility = "hidden";
				this.imagesList = null;
			}

	    	this.show = function(albumid) { // method triggered by click on album list
	    		var self = this;
	      		makeCall("GET", "GetImagesList?albumid=" + albumid, null,
					function(req) {
	          			if (req.readyState == 4) {
	            			var message = req.responseText;
	            			if (req.status == 200) {
	              				self.imagesList = JSON.parse(req.responseText);
	              				if (self.imagesList.length == 0) {
									console.log("No images");
	                				self.alert.textContent = "No images yet!";
	                				return;
	              				}
	              				self.update(); // images saved in imagesToShow
	          				}else {
								self.alert.textContent = message;
		   					}
						}
	      			});
	    	};

	    	this.update = function() { // rendilo parametrico per essere invocato anche da buttoni scorrimento
				// devo mostrare prime immagini (5 o lunghezza max) da ImagesList
				var imagesToShow = this.imagesList.slice(0,5);
				console.log(imagesToShow);
				
	      		this.container.innerHTML = ""; 

				var div,img,title;
	      		var self = this;
	      		
	      		imagesToShow.forEach(function(image) {
					div = document.createElement('div');
					div.classList.add("image_container");
					img = document.createElement('img');
					img.src = 'GetImage/' + image.path;
					img.addEventListener('mouseenter', () => imageDetails.show(image));
					title = document.createElement('h2');
					title.innerHTML = image.title;
					title.classList.add("image_title");
					div.appendChild(img)
					div.appendChild(title);
					self.container.appendChild(div);
	      		});
	      		this.container.style.visibility = "visible";
				imageDetails.reset();
	    		}
	  		}
			
			this.registerEvents = (_leftbutton,_rightbutton) => { // TO DO 
				this.leftButton = _leftbutton
				this.rightButton = _rightbutton;
				this.leftButton.style.display = "none";
				this.rightButton.style.display = "none";
				// if(...)
				this.leftButton.addEventListener("click", (e) => {
					var self = this;
				});
			}
			
		function ImageDetails(_alert,_imagecontainer,_commentcontainer){
			// mostra immagine e relativi commenti (gestisce anche creazione commento registerevents)
			this.alert = _alert;
			this.comment_container = _commentcontainer;
			this.imagecontainer = _imagecontainer;
			this.imageToShow;
			this.createBtn;

			this.reset = function (){
				this.imagecontainer.innerHTML ="";
				this.imagecontainer.style.visibility = "hidden";
				this.comment_container.style.visibility = "hidden";
				this.imageToShow = null;
				this.createBtn = null;
			}

			this.show = function(image) { // method triggered by hover on image list
			/*
			<div id="image_description">
			<img>
            <p >2022/01/01 19:05:58</p>
            <h2>Lago di Como</h2>
            <h3>Descrizione dell'immagine</h3>
          </div>
			*/
				console.log("mostro immagine");
				this.imagecontainer.innerHTML ="";
	      		var self = this;
	      		
	      		//this.imageToShow = image; credo non serva salvare l'immagine che si mostra
	      		var img,date,title,description;
	      		img = document.createElement('img');
	      		img.src = 'GetImage/' + image.path;
	      		img.setAttribute("id","fullscale_image");
	      			      	
	      		date = document.createElement('p');
	      		date.innerHTML = image.date;
	      		title = document.createElement('h2');
	      		title.innerHTML = image.title;
	      		description = document.createElement('h3');
	      		description.innerHTML = image.description;
	      		
				this.imagecontainer.append(img,date,title,description);
				
				this.imagecontainer.style.visibility = "visible";
				
				// recupero commenti 
				this.updateComments(image.id);
	      		// obj image as param, devo settare image id nel form commenti ?
				//this.comment_container.querySelector("input[type = 'hidden']").value = image.id;
	    	}
	    	
			this.updateComments = function(imageid){
				var self = this;
				makeCall("GET", "GetCommentsList?image=" + imageid, null,
				function(req) {
					if(req.readyState == 4) {
						var message = req.responseText;
						if(req.status == 200) {
							self.showComments(JSON.parse(message));
						} else {
							self.alert.textContent = message;
						}
					}
				})
			}
			
			this.showComments = function (arrayComments){
 					this.comment_container.innerHTML = ""; // empty the comment body
					
					var title, article, user, description;

					title = document.createElement("h2");
					title.innerHTML = "Commenti";
					this.comment_container.appendChild(title);

					var self = this;
					arrayComments.forEach(function(comment) {
						article = document.createElement("article");
						user = document.createElement("h2");
						user.innerHTML = comment.user;
						description = document.createElement("p");
						description.innerHTML = comment.text;
						article.append(user,description);
	
						self.comment_container.appendChild(article);
					});
					this.comment_container.style.visibility = "visible";
			}

			/*this.registerEvents = (_button) => { // creazione commento
				this.createBtn = _button;
				this.createBtn.addEventListener('click', (e) => {
	        var form = e.target.closest("form");
	        if (form.checkValidity()) {
	          var self = this;
	          makeCall("POST", 'CreateComment', form,
	            function(req) {
	              if (req.readyState == 4) {
	                var message = req.responseText;
	                if (req.status == 200) {
	                  imageDetails.show(self.imageToShow); // da controllare
	                } else {
	                  self.alert.textContent = message;
	                }
	              }
	            }
	          );
	        } else {
	          form.reportValidity();
	        }
	      });
		}*/
	}

}
