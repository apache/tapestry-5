Tapestry.Palette = Class.create();

Tapestry.Palette.prototype = {

  // TODO: Make Move Up/Move Down optional (via a subclass?)
  initialize : function(id) {
    // The two selects:
    
	  this.avail = $(id + ":avail");
	  this.selected = $(id);
	  
	  this.hidden = $(id + ":values");
	  
	  // Seperator used for values in the hidden field.
	  this.sep = ";";
	  
	  // The four BUTTON elements:
	  this.select = $(id + ":select");
	  this.deselect = $(id + ":deselect");
	  this.up = $(id + ":up");
	  this.down = $(id + ":down");
	  
	  this.bindEvents();   
  },  

  bindEvents : function() {
    var updateButtons = this.updateButtons.bindAsEventListener(this);
    Event.observe(this.avail, "change", updateButtons);
    Event.observe(this.selected, "change", updateButtons);
    
    var selectClicked = this.selectClicked.bindAsEventListener(this);
    Event.observe(this.select, "click", selectClicked);
    Event.observe(this.avail, "dblclick", selectClicked);
          
    var deselectClicked = this.deselectClicked.bindAsEventListener(this);      
    Event.observe(this.deselect, "click", deselectClicked);
    Event.observe(this.selected, "dblclick", deselectClicked);
    
    Event.observe(this.up, "click", this.moveUpClicked.bindAsEventListener(this));
    Event.observe(this.down, "click", this.moveDownClicked.bindAsEventListener(this));
  },
  
  updateButtons: function() {
    this.select.disabled = this.avail.selectedIndex < 0;
    
    var nothingSelected = this.selected.selectedIndex < 0;
    
    this.deselect.disabled = nothingSelected;
    this.up.disabled = nothingSelected || this.allSelectionsAtTop();
    this.down.disabled = nothingSelected || this.allSelectionsAtBottom();
  },  
  
  indexOfLastSelection : function(select) {
    if (select.selectedIndex < 0) return -1;
    
    for (var i = select.options.length - 1; i >= select.selectedIndex; i--) {
      if (select.options[i].selected) return i;
    }
    
    return -1;
  },
  
  allSelectionsAtTop: function() {
    var last = this.indexOfLastSelection(this.selected);
    var options = $A(this.selected.options);
    
    return ! options.slice(0, last).any(function (o) { return ! o.selected; });
  },
  
  allSelectionsAtBottom : function() {
    var options = $A(this.selected.options);

    // Make sure that all elements from the (first) selectedIndex to the end are also selected.     
    return options.slice(this.selected.selectedIndex).all(function(o) { return o.selected; });  
  },
  
  selectClicked : function(event) {
     this.transferOptions(this.avail, this.selected);
  },
  
  deselectClicked : function(event) {
     this.transferOptions(this.selected, this.avail);
  }, 
  
  transferOptions : function (from, to) {
    
    var toOptions = $A(to.options);
    var lastSelected = this.indexOfLastSelection(to);
    var before = lastSelected < 0 ? null : toOptions[toOptions.indexOf(lastSelected) + 1];
    
    toOptions.each(function(option) { option.selected = false; });
        
    var movers = this.removeSelectedOptions(from);
    this.moveOptions(movers, to, before);
    
  },
  
  updateHidden : function() {
    // Every value in the selected list (whether enabled or not) is combined to form the value.
    var values = $A(this.selected).map(function(o) { return o.value; });
    
    this.hidden.value = values.join(this.sep);
  },
  
  moveUpClicked : function(event) {
    var pos = this.selected.selectedIndex - 1;
    var movers = this.removeSelectedOptions(this.selected);

    var before = pos < 0 ? this.selected.options[0] : this.selected.options[pos];
 
    this.moveOptions(movers, this.selected, before);
    
    Event.stop(event);
  },
  
  removeSelectedOptions : function(select) {
    var movers = [];
    var options = select.options;
    
    for (var i = select.selectedIndex; i < select.length; i++) {
      var option = options[i];
      if (option.selected) {
        select.remove(i--);
        movers.push(option);  
      }          
    }
    
    return movers;
  },
  
  moveOptions : function(movers, to, before) {
    movers.each(function(option) { to.add(option, before); }.bind(this));
    
    this.updateHidden();  
    this.updateButtons();
  },
  
  moveDownClicked : function(event) {
    var lastSelected = $A(this.selected.options).reverse(true).find(function (option) { return option.selected; });
    var lastPos = lastSelected.index;
    var before = this.selected.options[lastPos + 2];
        
    this.moveOptions(this.removeSelectedOptions(this.selected), this.selected, before);
    
    Event.stop(event);
  }
};



