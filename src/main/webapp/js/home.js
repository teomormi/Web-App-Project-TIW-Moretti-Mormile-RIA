{ // avoid variables ending up in the global scope
	// page components
	let headerName, albumsList, imageDetails, imagesList, uploadImage,createAlbum, pageOrchestrator = new PageOrchestrator();


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
			
			//mostro il nome dell'utente loggato
			headerName = new HeaderName(sessionStorage.getItem('user'), document.getElementById("user_name"));
			headerName.show();

			// need the orchestrator when refresh al pages
			//creo il componente che permette all'utente di creare un nuovo album
			createAlbum = new CreateAlbum(alertContainer);
			//registro l'evento di click sul bottone creaAlbum
			createAlbum.registerEvents(document.getElementById("button_createalbum"),this);
			
			//creo il componente che permette all'utente di uploadare una nuova immagine da locale
			uploadImage = new UploadImage(
				alertContainer,
				document.getElementById("minealbums_checkbox"));
			//registro l'evento di click sul bottone che mi permette di caricare l'immagine
			uploadImage.registerEvents(document.getElementById("button_uploadimage"));
			
			//creo il componente che permette all'utente di visualizzare la lista degli album
			albumsList = new AlbumsList(
				alertContainer,
				document.getElementById("albums_container"),
				document.getElementById("albums_containerbody"));
			//registro l'evento sul click del bottone save list, che mi permette di salvare l'ordine di apparizione degli album
			albumsList.registerEvents(document.getElementById("save_list"));
			
			//creo il componente che permette all'utente di visualizzare la lista delle immagini presenti nell'album'
			imagesList = new ImagesList(
				alertContainer,
				document.getElementById("album_images"),
				document.getElementById("album_name"));
			//registro gli eventi di click sul bottone che mostrano le immgini precedenti o successive a quelle che sto visualizzando
			imagesList.registerEvents(document.getElementById("button_left"), document.getElementById("button_right"));
			
			//creo il componente che mostra i dettagli (titolo, data e commenti) della foto selezionata dall'utente
			imageDetails = new ImageDetails(
				alertContainer,
				document.getElementById("image_details"),
				document.getElementById("image_description"),
				document.getElementById("comment_container"),
				document.getElementById("new_comment"));
			//registro l'evento di click sul bottone che permette di inserire un nuovo commento
			imageDetails.registerEvents(document.getElementById("button_comment"));
			
			//aggiungo l'evento di click sul logout
			document.getElementById("button_logout").addEventListener('click', () => {
				window.sessionStorage.removeItem('user'); //rimuove l'oggetto user dall'utente dalla sessione
				window.location.href = "index.html"; //ci rimanda alla pagina index.html (pagina di login/registrazione)
			})
		};
		
		/*con le funzioni di reset nascondo i componenti ed invoco 
		la funzione di albumList show in modo da mostrare simulando un autoclick sul
		primo album della lista in modo da mostrare le immagini del primo album*/
		this.refresh = function(currentAlbum) {
			alertContainer.textContent = "";
			albumsList.reset();
			imagesList.reset();
			imageDetails.reset();
			uploadImage.reset();

			albumsList.show(function() {
				albumsList.autoclick(currentAlbum);
			});
		};
	}
	
	/*i parametri sono passati prendendo i dati relativi all'utente dalla sessione*/
	function HeaderName(_username, _namecontainer) {
		this.username = _username;
		this.namecontainer = _namecontainer;
		
		/*funzione che serve per aggiornare nella home page il nome dell'utente loggato*/
		this.show = function() {
			this.namecontainer.textContent = this.username;
		}
	}
	
	/*i parametri passati sono oggetti "presi" tramite la funzione getElementById che preleva gli elementi 
	dalla pagina HTML prendendo quelli con l'id specificato'*/
	function AlbumsList(_alert, _albums_container, _albums_containerbody) { // add album name
		this.alert = _alert;
		this.albums_container = _albums_container;
		this.albums_containerbody = _albums_containerbody;
		this.saveBtn;
		this.startElement;
		
		/*nascondo il componente contenente la lista degli album e resetto start element */
		this.reset = function() {
			this.albums_container.style.visibility = "hidden";
			this.startElement = null;
		}
		
		/*registra l'azione di click sul bottone per il salvataggio dell'ordinamento degli album dell'utente loggato */
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
					success: function(data){},
					error: function(xhr, textStatus, error) {
						self.alert.textContent = error;
					}
				});
				this.saveBtn.style.display = "none";
			});
		}
		
		/*funzione che interroga il DB che permette di recuperare la lista degli album*/
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

		/*funzione che permette di aggiornare le componenti visive della lista degli album e 
		della form di caricamento immagine*/
		this.update = function(arraysAlbums) {
			console.log(arraysAlbums[0]); // others albums
			console.log(arraysAlbums[1]); //yours album
			
			/*svuoto il contenitore della lista degli album e lo ricostruisco */
			this.albums_containerbody.innerHTML = ""; // empty the table body
			var self = this;

			/*insrisco il titolo nell'apposita riga della tabella contenente la lista degli album  */
			this.albums_containerbody.appendChild(this.createTitle("OTHER ALBUMS"));

			arraysAlbums[0].forEach(function(album) { // show other albums
				//creo una riga per volta e la "appendo" al documento HTML
				row = self.createItem(album);
				self.albums_containerbody.appendChild(row);
			});
			
			/*insrisco il titolo nell'apposita riga della tabella contenente la lista degli album  */
			this.albums_containerbody.appendChild(this.createTitle("YOUR ALBUMS"));
			
			
			arraysAlbums[1].forEach(function(album) { // show your albums
			//creo una riga per volta e la "appendo" al documento HTML
				row = self.createItem(album);
				row.draggable = true;
				row.classList.add('draggable')
				row.addEventListener("dragstart", self.dragStart); //save dragged element reference
				row.addEventListener("dragover", self.dragOver); // change color of reference element to red
				row.addEventListener("dragleave", self.dragLeave); // change color of reference element to black
				row.addEventListener("drop", self.drop); //change position of dragged element using the referenced element
				self.albums_containerbody.appendChild(row);
				//aggiungo la classe 'draggable' e i vari eventi, in modo tale da poter modificare l'ordine 
				//degli album tramite drag and drop
			});

			this.albums_containerbody.style.visibility = "visible";

			// send your albums to list of checkbox in uploadimage 
			uploadImage.updateCheckbox(arraysAlbums[1]);
		}

		this.createTitle = function(message) {
			//creo la riga dedicata al titolo
			var rowtitle, tdtitle, title, text;
			rowtitle = document.createElement("tr");
			tdtitle = document.createElement("td");
			title = document.createElement("h2");
			text = document.createTextNode(message);
			title.appendChild(text);
			tdtitle.appendChild(title);
			rowtitle.appendChild(tdtitle);
			return rowtitle;
		}

		this.createItem = function(albumitem) {
			//creo le varie righe contenenti i titoli degli album
			var rowitem, tditem, text;
			rowitem = document.createElement("tr");
			tditem = document.createElement("td");
			text = document.createTextNode(albumitem.title);
			tditem.appendChild(text);
			rowitem.setAttribute('albumid', albumitem.id); // set a custom HTML attribute
			//aggiungo l'evento click sulla riga in modo tale che quando l'utente clicca
			//sul titolo dell'album vengano mostrate 5 immagini dell'album 
			//tramite la funzione show della "classe" imagesList
			rowitem.addEventListener("click", () => imagesList.show(albumitem));
			rowitem.appendChild(tditem);
			rowitem.classList.add("album");
			return rowitem;
		}
		
		this.dragStart = (e) => {
			//salvo in una variabile l'oggetto su cui è iniziato il drag
			this.startElement = e.target.closest("tr");
		}

		this.dragOver = (e) => {
			//evita che si svolga l'azione predefinita legata all'evento se esso non viene gestito in maniera
			//esplicita
			e.preventDefault();
			/*fa capire all'utente su che cella ci stiamo posizionando (cambia lo stile con CSS) */
			var dest = e.target.closest("tr");
			dest.classList.add('selected');
		}

		this.dragLeave = (e) => {
			/*Non sono più su quell'oggetto quindi tolgo la classe 'selected' (modifico lo stile) */
			var dest = e.target.closest("tr");
			dest.classList.remove('selected');
		}

		this.drop = (e) => {
			var dest = e.target.closest("tr"); //salvo oggetto su cui lascio "cadere" l'oggetto che sto trascinando
			var table = dest.closest('table'); //prendo la tabella da aggiornare
			var rowsArray = Array.from(table.querySelectorAll('tr.draggable')); //prendo tutti i tr con classe draggable
			var indexDest = rowsArray.indexOf(dest) //ottengo la posizione di destinazione
			if (rowsArray.indexOf(this.startElement) != indexDest) { //ho effettivamente spostato l'oggetto
				if (rowsArray.indexOf(this.startElement) < indexDest)
					//sposto verso il basso, lo metto sopra a quello selezionato
					this.startElement.parentElement.insertBefore(this.startElement, rowsArray[indexDest + 1]);
				else if (rowsArray.indexOf(this.startElement) > indexDest)
					//sposto dal basso in alto, allora metto al posto di quello su cui l'ho droppato
					this.startElement.parentElement.insertBefore(this.startElement, rowsArray[indexDest]);
				this.saveBtn.style.display = "block"; // show save button
			}
			this.unselectRows(rowsArray);
		}

		this.unselectRows = function(rowsArray) {
			//tolgo la classe selected a tutte le righe in modo che tornino con lo stile originale
			for (var i = 0; i < rowsArray.length; i++) {
				rowsArray[i].classList.remove('selected');
			}
		}

		this.getAlbumsOrder = () => {
			var arraytr = Array.from(this.albums_containerbody.querySelectorAll("tr.draggable"));
			/*ottengo un array contenente tutti i tr che possono essere spostati (avrò quindi un array 
			con gli album dell'utente loggato), con la seconda istruzione invece genero quello che sarà
			il nostro array di sorting. */
			return arraytr.map(a => a.getAttribute("albumid"));
			/*la funzione sostituisce nella cella dell'array al posto dell'oggetto a il suo id in modo tale
			che ho la coppia pos-id e so in che ordine l'utente ha salvato i prorpri album */
		}
		
		/*
		simula il click sulla prima riga della tabella contenente la lista degli alvum in modo tale che sul click del row 
		invoco la show per la lista delle immagin (registrata alla creazione)
		*/
		this.autoclick = function(albumId) { // simulate the click on album
			var e = new Event("click");
			var selector = "tr[albumid='" + albumId + "']";
			var rowToClick =  // the first album or the album with id = albumId
				(albumId) ? document.querySelector(selector) : this.albums_containerbody.querySelectorAll("tr")[1];
			if (rowToClick) rowToClick.dispatchEvent(e);
		}
	}


	function ImagesList(_alert, _container,_titlecontainer) {
		this.alert = _alert;
		this.container = _container;
		this.titlecontainer = _titlecontainer;
		this.leftButton;
		this.rightButton;
		this.imagesList; // the complete list of images
		this.firstIndex;
		
		//nasconde il contenitore delle immagini e resetta la lista delle immagini
		this.reset = function() {
			this.container.style.visibility = "hidden";
			this.imagesList = null;
		}
		
		/*questo metodo viene invocato quando si ha il click su un album */
		this.show = function(album) { // method triggered by click on album list
			var self = this;
			makeCall("GET", "GetImagesList?albumid=" + album.id, null, //passo come parametro l'id dell'album cliccato
			//in modo che la servlet possa recuperare dal DB le immagini dell'album selezionato
				function(req) { //funzione di callback
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							self.imagesList = JSON.parse(req.responseText);
							self.firstIndex = 0;
							self.update(); // images saved in imagesToShow
							//una volta terminata la chiamata mostro le 5 immagini
						} else {
							self.alert.textContent = message;
						}
					}
				});
			// mostra anche titolo album
			this.titlecontainer.innerHTML = "";
			var title = document.createElement("h1");
			title.innerHTML = album.title;
			this.titlecontainer.appendChild(title);
		};
		
		
		/*la funzione ci permette di mostrare 5 immagini dell'album e gestisce anche il mostrare/nascondere 
		degi bottini next e prev */
		this.update = function() { // 5 images from index
			
			//prende le 5 immagini partendo da firstIndex (variabile della "classe")
			var imagesToShow = this.imagesList.slice(this.firstIndex, this.firstIndex + 5);
			console.log(imagesToShow);

			this.container.innerHTML = "";

			
			var self = this;
			var div,img,title;

			imagesToShow.forEach(function(image) {
				div = document.createElement('div');
				div.classList.add("image_container");
				img = document.createElement('img');
				img.src = 'GetImage/' + image.path; 
				//aggiungo evento nel caso mi posizioni sull'immagine, ciamo funzione 'update'
				img.addEventListener('mouseenter', () => imageDetails.update(image));
				//genero i vari componenti per la pagina HTML e li "appendo"
				title = document.createElement('h2');
				title.innerHTML = image.title;
				title.classList.add("image_title");
				div.appendChild(img)
				div.appendChild(title);
				self.container.appendChild(div);
			});
			this.container.style.visibility = "visible";
			// controllo se devo mostrare pulsanti
			if (this.firstIndex > 0)
				this.leftButton.style.display = "block";
			else
				this.leftButton.style.display = "none";
			if ((this.firstIndex + 5) < this.imagesList.length)
				this.rightButton.style.display = "block";
			else
				this.rightButton.style.display = "none";
			imageDetails.reset();
		}

		/*aggiunge eventi ai bottoni prev e next, quando ho il click su di loro viene richiamata update
		che aggiorna le foto mostrate */
		this.registerEvents = function(_leftbutton, _rightbutton) { 
			this.leftButton = _leftbutton;
			this.rightButton = _rightbutton;
			this.leftButton.style.display = "none";
			this.rightButton.style.display = "none";

			this.leftButton.addEventListener("click", (e) => {
				//aggiorno indice prima foto da mostrare
				this.firstIndex = this.firstIndex - 5;
				console.log(this.firstIndex);
				this.update();
			});

			this.rightButton.addEventListener("click", (e) => {
				//aggiorno indice prima foto da mostrare
				this.firstIndex = this.firstIndex + 5;
				console.log(this.firstIndex);
				this.update();
			});
		}
	}

	function ImageDetails(_alert, _maincontainer, _imagecontainer, _commentcontainer, _createcontainer) {
		// mostra immagine e relativi commenti (gestisce anche creazione commento registerevents)
		this.alert = _alert;
		this.maincontainer = _maincontainer
		this.comment_container = _commentcontainer;
		this.imagecontainer = _imagecontainer;
		this.createcontainer = _createcontainer;
		this.imageToShow;
		this.createBtn;

		this.reset = function() {
			this.imagecontainer.innerHTML = "";
			this.maincontainer.style.visibility = "hidden";
			this.imageToShow = null;
			this.createBtn = null;
		}

		this.update = function(image) { // method triggered by hover on image list

			this.imagecontainer.innerHTML = "";
			var self = this;

			this.imageToShow = image;
			var img, date, title, description;
			img = document.createElement('img');
			img.src = 'GetImage/' + image.path; 
			img.setAttribute("id", "fullscale_image");

			date = document.createElement('p');
			date.innerHTML = image.date;
			title = document.createElement('h2');
			title.innerHTML = image.title;
			description = document.createElement('h3');
			description.innerHTML = image.description;

			this.imagecontainer.append(img, date, title, description);

			// mostro commenti relativi all'immagine attiva
			this.showComments(image.id);

			// setto image id nel form commenti 
			this.createcontainer.querySelector("input[type = 'hidden']").value = image.id;


		}
		
		/*funzione che interroga il DB per recuperare i commenti relativi all'immagine. 
		Richiamata nella funzione update
		 */
		this.showComments = function(imageid) {
			var self = this;
			makeCall("GET", "GetCommentsList?image=" + imageid, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							self.updateComments(JSON.parse(message));
						} else {
							self.alert.textContent = message;
						}
					}
				})
		}

		this.updateComments = function(arrayComments) {
			var title, article, user, description;
			this.comment_container.innerHTML = ""; // empty the comment body


			title = document.createElement("h2");
			if (arrayComments.length > 0)
				title.innerHTML = "Commenti";
			else
				title.innerHTML = "Nessun commento a questa immagine";
			this.comment_container.appendChild(title);

			var self = this;
			
			//compongo un articolo per ogni commento, e lo "appendo" nel documento HTML
			arrayComments.forEach(function(comment) {
				article = document.createElement("article");
				user = document.createElement("h2");
				user.innerHTML = comment.user;
				description = document.createElement("p");
				description.innerHTML = comment.text;
				article.append(user, description);

				self.comment_container.appendChild(article);
			});

			this.maincontainer.style.visibility = "visible";
		}
		
		/*registro l'evento per creare il bottone */
		this.registerEvents = (_button) => {
			this.createBtn = _button;
			this.createBtn.addEventListener('click', (e) => {
				var form = e.target.closest("form");
				console.log(form);
				if (form.checkValidity()) {
					var self = this;
					makeCall("POST", 'CreateComment', form,
						function(req) { //funzione di callback eseguita a ogni cambio di stato della requsest
							if (req.readyState == 4) {
								var message = req.responseText;
								if (req.status == 200) {
									self.showComments(self.imageToShow.id);
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
		}
	}

	function UploadImage(_alert, _checkboxcontainer) {
		this.alert = _alert;
		this.checkboxcontainer = _checkboxcontainer;
		this.createBtn;

		this.reset = function() {
			this.checkboxcontainer.innerHTML = "";
			this.createBtn = null;
		}
		
		/*crea le varie checkbox da inserire nel form dell'upload dell'immagine */
		this.updateCheckbox = function(mineAlbums) {
			var title, checkbox;
			var self = this;
			
			mineAlbums.forEach(function(album) {
				checkbox = document.createElement("input");
				checkbox.type = 'checkbox';
				checkbox.value = album.id;
				checkbox.name = 'albums';
				title = document.createElement("label");
				title.innerHTML = album.title;
				self.checkboxcontainer.append(checkbox, title,document.createElement("br"));
			});
		}
		
		/*registra l'evento di click sul bottone dell'update dell'immagine */		
		this.registerEvents = (_button) => {
			this.createBtn = _button;
			this.createBtn.addEventListener('click', (e) => {
				var form = e.target.closest("form");
				console.log(form);
				if (form.checkValidity()) {
					var self = this;
					makeCall("POST", 'CreateImage', form,
						function(req) {
							if (req.readyState == 4) {
								var message = req.responseText;
								if (req.status != 200) {
									self.alert.textContent = message;
								}
							}
						}
					);
				} else {
					form.reportValidity();
				}
			});
		}
	}
	
	function CreateAlbum(_alert){
		this.alert = _alert;
		this.createBtn;

		this.reset = function() {
			this.createBtn = null;
		}
		
		/*registro l'evento click sul bottone per creare l'album in modo che al click eseguo una chiamata
		col metodo post alla servlet CreateAlbum */
		this.registerEvents = function(_button,orchestrator) {
	      this.createBtn = _button;
		  this.createBtn.addEventListener('click', (e) => {
	        	var self = this;
	        	/*eseguo la chiamata alla servlet passando la form "più vicina al bottone cliccato" in cui sono
	        	presenti i dati dell'album'*/
	          makeCall("POST", 'CreateAlbum', e.target.closest("form"),
	            //funzione di call back che viene invocata ogni volta che si ha un cambio di stato nella richiesta
	            function(req) {
	              if (req.readyState == XMLHttpRequest.DONE) {
	                var message = req.responseText; 
	                if (req.status == 200) {
						//nel caso in cui la richiesta si finita e la risposta è positiva (ok==200) allora aggiorno
						//questa funzione richiama l'autoclick passando come parametro l'id dell'album appena creato
						//in modo tale che esso venga subito mostrato
	                  orchestrator.refresh(message); // id of the new album passed
	                } else {
	                  self.alert.textContent = message;
	                }
	              }
	            }
	          );
	        });
	      }
	}
}
