
var ex_references = {

    data : null,
    
    queue : [],

    // this is callback for JSONP loading data from the spreadsheet
    loadReferences : function(data) {
        this.data = new ListFeed(data, ["ID", "NAME", "DESCR", "LINK", "TAGS"]);
        if (this.queue.length > 0) {
            for (var i = 0; i < this.queue.length; i++)
                this.processSlide(this.queue[i]);
            this.queue = [];
        }
    },
    
    // event handler for slideprocess event
    processSlide : function(slide) {
        
        var ext = this;
        
        // process a single reference
        var processRef = function(div) {
            for (var i = 0; i < ex_references.data.length; i++) {
                var row = ext.data.getRow(i);
                if (row.id.toLowerCase() == div.id) { 
                    div.className += " extref";
                    var name = (!div.innerHTML || div.innerHTML === "" ? row.name : div.innerHTML); 
                    div.innerHTML = "<a class=\"ext-link ref\" title=\"" + row.descr + 
                        "\" target=\"humla_reference\" href=\"" + row.link + "\">" + name + "</a>";
                    return;
                }
            }
        };

        // find all elements with h-ref classname
        if (this.data)  {
            var divs = slide.element.getElementsByClassName("h-ref");
            for (var i = 0; i < divs.length; i++)
                processRef(divs[i]);
        } else {
            // it may happen that slide processing will occur prior to data is available
            // so queue the processing
            this.queue.push(slide);
        }
    }
    
};

