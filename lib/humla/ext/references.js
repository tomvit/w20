
var ex_references = {

    data : null,
    
    queue : [],

    // this is callback for JSONP loading data from the spreadsheet
    loadReferences : function(data) {
        this.data = data.values
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
                var row = ext.data[i];
                if (row.length == 4 && row[0].toLowerCase() == div.id) { 
                    div.className += " extref";
                    var name = (!div.innerHTML || div.innerHTML === "" ? row[1] : div.innerHTML); 
                    div.innerHTML = "<a class=\"ext-link ref\" title=\"" + row[2] + 
                        "\" target=\"humla_reference\" href=\"" + row[3] + "\">" + name + "</a>";
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

