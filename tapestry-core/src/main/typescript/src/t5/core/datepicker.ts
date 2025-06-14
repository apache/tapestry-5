/*----------------------------------------------------------------------------\
|                              Date Picker 1.06                               |
|-----------------------------------------------------------------------------|
|                         Created by Erik Arvidsson                           |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
|                            A DOM based Date Picker                          |
|-----------------------------------------------------------------------------|
|       Copyright (c) 1999, 2002, 2002, 2003, 2004, 2006 Erik Arvidsson       |
|-----------------------------------------------------------------------------|
| Licensed under the Apache License, Version 2.0 (the "License"); you may not |
| use this file except in compliance with the License.  You may obtain a copy |
| of the License at http://www.apache.org/licenses/LICENSE-2.0                |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| Unless  required  by  applicable law or  agreed  to  in  writing,  software |
| distributed under the License is distributed on an  "AS IS" BASIS,  WITHOUT |
| WARRANTIES OR  CONDITIONS OF ANY KIND,  either express or implied.  See the |
| License  for the  specific language  governing permissions  and limitations |
| under the License.                                                          |
|-----------------------------------------------------------------------------|
| Dependencies: datepicker.css      Date picker style declarations            |
|-----------------------------------------------------------------------------|
| 2002-02-10 | Changed _update method to only update the text nodes instead   |
|            | rewriting the entire table. Also added support for mouse wheel |
|            | in IE6.                                                        |
| 2002-01-14 | Cleaned up for 1.0 public version                              |
| 2002-01-15 | Replace all innerHTML calls with DOM1 methods                  |
| 2002-01-18 | Minor IE6 bug that occured when dragging the mouse             |
| 2002-01-19 | Added a popup that is shown when the user clicks on the month. |
|            | This allows navigation to 6 adjacent months.                   |
| 2002-04-10 | Fixed a bug that occured in the popup when a date was selected |
|            | that caused surroundung months to "overflow"                   |
|            | This had the effect that one could get two October months      |
|            | listed.                                                        |
| 2002-09-06 | I had missed one place were window was used instead of         |
|            | doc.parentWindow                                               |
| 2003-08-28 | Added support for ensurin no date overflow when changing       |
|            | months.                                                        |
| 2004-01-10 | Adding type on the buttons to ensure they are not submit       |
|            | buttons. Minor CSS change for CSS2                             |
| 2006-05-28 | Changed license to Apache Software License 2.0.                |
| 2011-07-27 | Separated "selected date" and "calendar date" concepts.        |
|            | Selected date is the date specifically selected by the user to |
|            | put into the form field.  Calendar date reflects the currently |
|            | displayed month. These are often, but not always, the same     |
|            | value.  Separating them simplifies a lot of logic and resolves |
|            | TAP5-1409. Also somewhat smarter for whether to trigger        |
|            | onselect when clicking "today" (and/or "none")                 |
| 2012-11-11 | Minor changes to integrate into a page with Twitter Bootstrap, |
|            | and to support localizing the Today/None buttons.              |
| 2013-08-29 | More changes to adapt to Bootstrap 3 style                     |
| 2014-04-27 | Wrap the datepicker script into an AMD module                  |
|-----------------------------------------------------------------------------|
| Created 2001-10-?? | All changes are in the log above. | Updated 2006-05-28 |
\----------------------------------------------------------------------------*/

// @ts-nocheck

  //The DatePicker constructor
  //oDate : Date Optional argument representing the date to select
  //Note: some minor modifications for Tapestry, to work well as a popup.
  function DatePicker(oDate)
  {
      // check arguments
      if (arguments.length == 0)
      {
          this._selectedDate = null;
          this._calendarDate = new Date;
          this._selectedInited = false;
      }
      else
      {
          this._selectedDate = oDate;
          if (!oDate) 
          {
              this._calendarDate = new Date;
          } else 
          {
              this._calendarDate = new Date(oDate);
          }
          this._selectedInited = true;
          
      }

      this._matrix = [[],[],[],[],[],[],[]];
      this._showNone = true;
      this._showToday = true;
      this._firstWeekDay = 0; // start week with monday according to standards
      this._redWeekDay = 6;   // sunday is the default red day.
      // the names of the months and days
      this._months = [
          "January", "February", "March", "April",
          "May", "June", "July", "August",
          "September", "October", "November", "December"];
      this._days = ["m", "t", "w", "t", "f", "s", "s"];

      // Allow these to be localized
      this._todayLabel = "Today";
      this._noneLabel = "None";

  }

  // Function invoked whenever the selected date changes, whether by
  // navigation or when the user selects a date.
  DatePicker.prototype.onchange = function ()
  {
  };

  // onselect is more specified than onchange, and    is triggered only when the user makes a specific selection
  // using the calendar (rather than navigating to a new month). For Tapestry,
  // this will dismiss the popup.
  DatePicker.prototype.onselect = function()
  {
  }


  // create the nodes inside the date picker
  DatePicker.prototype.create = function (doc)
  {
      if (doc == null) doc = document;

      this._document = doc;

      // create elements
      this._el = doc.createElement("div");
      this._el.className = "datePicker";

      // header
      var div = doc.createElement("div");
      div.className = "header";
      this._el.appendChild(div);

      var headerTable = doc.createElement("table");
      headerTable.className = "headerTable";
      headerTable.cellSpacing = 0;
      div.appendChild(headerTable);

      var tBody = doc.createElement("tbody");
      headerTable.appendChild(tBody);

      var tr = doc.createElement("tr");
      tBody.appendChild(tr);

      var td = doc.createElement("td");
      this._previousMonth = doc.createElement("button");
      this._previousMonth.className = "btn btn-default btn-xs previousButton";
      this._previousMonth.setAttribute("type", "button");
      var icon = doc.createElement("span");
      icon.className = "glyphicon glyphicon-chevron-left";
      this._previousMonth.appendChild(icon);
      td.appendChild(this._previousMonth);
      tr.appendChild(td);

      td = doc.createElement("td");
      td.className = "labelContainer";
      tr.appendChild(td);

      this._topLabel = doc.createElement("a");
      this._topLabel.className = "topLabel";
      this._topLabel.href = "#";
      this._topLabel.appendChild(doc.createTextNode(String.fromCharCode(160)));
      td.appendChild(this._topLabel);

      this._labelPopup = doc.createElement("div");
      this._labelPopup.className = "labelPopup";
      // no insertion

      td = doc.createElement("td");
      this._nextMonth = doc.createElement("button");
      this._nextMonth.className = "btn btn-default btn-xs nextButton";
      this._nextMonth.setAttribute("type", "button");
      icon = doc.createElement("span");
      icon.className = "glyphicon glyphicon-chevron-right";
      this._nextMonth.appendChild(icon);

      td.appendChild(this._nextMonth);
      tr.appendChild(td);

      // grid
      div = doc.createElement("div");
      div.className = "grid";
      this._el.appendChild(div);
      this._table = div;

      // footer
      div = doc.createElement("div");
      div.className = "footer";
      this._el.appendChild(div);

      var footerTable = doc.createElement("table");
      footerTable.className = "footerTable";
      footerTable.cellSpacing = 0;
      div.appendChild(footerTable);

      tBody = doc.createElement("tbody");
      footerTable.appendChild(tBody);

      tr = doc.createElement("tr");
      tBody.appendChild(tr);

      td = doc.createElement("td");
      this._todayButton = doc.createElement("button");
      this._todayButton.className = "btn btn-default btn-xs";
      this._todayButton.setAttribute("type", "button");
      this._todayButton.appendChild(doc.createTextNode(this._todayLabel));
      td.appendChild(this._todayButton);
      tr.appendChild(td);

      td = doc.createElement("td");
      td.className = "filler";
      td.appendChild(doc.createTextNode(String.fromCharCode(160)));
      tr.appendChild(td);

      td = doc.createElement("td");
      this._noneButton = doc.createElement("button");
      this._noneButton.className = "btn btn-default btn-xs";
      this._noneButton.setAttribute("type", "button");
      this._noneButton.appendChild(doc.createTextNode(this._noneLabel));
      td.appendChild(this._noneButton);
      tr.appendChild(td);


      this._createTable(doc);

      this._updateTable();
      this._setTopLabel();

      if (!this._showNone)
          this._noneButton.style.visibility = "hidden";
      if (!this._showToday)
          this._todayButton.style.visibility = "hidden";

      // IE55+ extension
      this._previousMonth.hideFocus = true;
      this._nextMonth.hideFocus = true;
      this._todayButton.hideFocus = true;
      this._noneButton.hideFocus = true;
      // end IE55+ extension

      // hook up events
      var dp = this;
      // buttons
      this._previousMonth.onclick = function ()
      {
          dp.goToPreviousMonth();
      };
      this._nextMonth.onclick = function ()
      {
          dp.goToNextMonth();
      };
      this._todayButton.onclick = function ()
      {
          dp.goToToday();
      };
      this._noneButton.onclick = function ()
      {
          //this should always clear the date and trigger onselected... 
          dp.setDate(null, true);
      };

      this._el.onselectstart = function ()
      {
          return false;
      };

      this._table.onclick = function (e)
      {
          // find event
          if (e == null) e = doc.parentWindow.event;

          // find td
          var el = e.target != null ? e.target : e.srcElement;
          while (el.nodeType != 1)
              el = el.parentNode;
          while (el != null && el.tagName && el.tagName.toLowerCase() != "td")
              el = el.parentNode;

          // if no td found, return
          if (el == null || el.tagName == null || el.tagName.toLowerCase() != "td")
              return;

          var d = new Date(dp._calendarDate);
          var n = Number(el.firstChild.data);
          if (isNaN(n) || n <= 0 || n == null)
              return;

          d.setDate(n);
          dp.setDate(d);
      };

      // show popup
      this._topLabel.onclick = function (e)
      {
          dp._showLabelPopup();
          return false;
      };

      this._el.onkeydown = function (e)
      {
          if (e == null) e = doc.parentWindow.event;
          var kc = e.keyCode != null ? e.keyCode : e.charCode;

          if (kc < 37 || kc > 40) return true;

          var d = new Date(dp._calendarDate).valueOf();
          if (kc == 37) // left
              d -= 24 * 60 * 60 * 1000;
          else if (kc == 39) // right
              d += 24 * 60 * 60 * 1000;
          else if (kc == 38) // up
              d -= 7 * 24 * 60 * 60 * 1000;
          else if (kc == 40) // down
              d += 7 * 24 * 60 * 60 * 1000;

          dp.setCalendarDate(new Date(d));
          return false;
      }

      // ie6 extension
      this._el.onmousewheel = function (e)
      {
          if (e == null) e = doc.parentWindow.event;
          var n = - e.wheelDelta / 120;
          var d = new Date(dp._calendarDate);
          var m = d.getMonth() + n;
          d.setMonth(m);


          dp.setCalendarDate(d);

          return false;
      }

      doc.onclick  =  function (e) {
          var targ;
          
           // find event
          if (e == null) e = doc.parentWindow.event;
          
          if (e.target) targ = e.target;
          else if (e.srcElement) targ = e.srcElement;
          // find classname 'datePicker' as parent
          var insideDatePicker = null;
          var parent = targ.parentNode;
          while (parent != null) {
              if (parent.className == 'datePicker' || parent.className == 'labelPopup') {
                  insideDatePicker = parent;
                  break;
              }
              parent = parent.parentNode;
          }
      }
      return this._el;
  };

  DatePicker.prototype.setCalendarDate = function(oDate)
  {
      if (oDate != null) 
      {
          //note that calendarDate should never be null!
          this._calendarDate = oDate;
      }
      this._hideLabelPopup();
      this._setTopLabel();
      this._updateTable();
  }

  DatePicker.prototype.setDate = function (oDate, forceOnSelect)
  {

      // if null then set None
      if (oDate == null)
      {
          //if _selectedDate isn't null, then this is an actual change...
          //but if it /is/ null, we have to see if we were inited or not. If we weren't inited, then we're 
          //setting this to null now, and we shouldn't fire a select...
          //but the problem occurs on subsequent... hm...
          if (this._selectedDate != null)
          {
              this._selectedDate = null;
              if (typeof this.onchange == "function")
                  this.onchange();
              this.onselect();
          } else if (forceOnSelect)
              this.onselect();
          //note: setDate must inherently set the calendar date
          this._selectedInited=true;
          this.setCalendarDate(null);

          return;
      }

      // if string or number create a Date object
      if (typeof oDate == "string" || typeof oDate == "number")
      {
          oDate = new Date(oDate);
      }

      // do not update if not really changed
      if (this._selectedDate == null || !this._datesAreSame(this._selectedDate, oDate))
      {
          this._selectedDate = new Date(oDate);
      
          if (typeof this.onchange == "function")
              this.onchange();

          //so if _selectedInited is false, then the value is different only because we set the value programmatically, post-initialization.
          //that handles the creation + set event. Subsequent reveals will set it to whatever _selectedDate already was, so it's handled.
          if (this._selectedInited)
              this.onselect();
          else
              this._selectedInited=true;
      } else if (forceOnSelect)
          this.onselect();
      //note: setDate must inherently set the calendar date
      this.setCalendarDate(oDate);

  }


  DatePicker.prototype.getDate = function ()
  {
      if (!this._selectedDate) return null;
      return new Date(this._selectedDate);    // create a new instance
  }

  // creates the table elements and inserts them into the date picker
  DatePicker.prototype._createTable = function (doc)
  {
      var str, i;
      var rows = 6;
      var cols = 7;
      var currentWeek = 0;

      var table = doc.createElement("table");
      table.className = "gridTable";
      table.cellSpacing = 0;

      var tBody = doc.createElement("tbody");
      table.appendChild(tBody);

      // days row
      var tr = doc.createElement("tr");
      tr.className = "daysRow";

      var td, tn;
      var nbsp = String.fromCharCode(160);
      for (i = 0; i < cols; i++)
      {
          td = doc.createElement("td");
          td.appendChild(doc.createTextNode(nbsp));
          tr.appendChild(td);
      }
      tBody.appendChild(tr);

      // upper line
      tr = doc.createElement("tr");
      td = doc.createElement("td");
      td.className = "upperLine";
      td.colSpan = 7;
      tr.appendChild(td);
      tBody.appendChild(tr);

      // rest
      for (i = 0; i < rows; i++)
      {
          tr = doc.createElement("tr");
          for (var j = 0; j < cols; j++)
          {
              td = doc.createElement("td");
              td.appendChild(doc.createTextNode(nbsp));
              tr.appendChild(td);
          }
          tBody.appendChild(tr);
      }
      str += "</table>";

      if (this._table != null)
          this._table.appendChild(table)
  };
  // this method updates all the text nodes inside the table as well
  // as all the classNames on the tds
  DatePicker.prototype._updateTable = function ()
  {
      // if no element no need to continue
      if (this._table == null) return;

      var i;
      var str = "";
      var rows = 6;
      var cols = 7;
      var currentWeek = 0;

      var cells = new Array(rows);
      this._matrix = new Array(rows)
      for (i = 0; i < rows; i++)
      {
          cells[i] = new Array(cols);
          this._matrix[i] = new Array(cols);
      }

      // Set the tmpDate to this month
      var tmpDate = new Date(this._calendarDate.getFullYear(),
              this._calendarDate.getMonth(), 1);
      var today = new Date();
      // go thorugh all days this month and store the text
      // and the class name in the cells matrix
      for (i = 1; i < 32; i++)
      {
          tmpDate.setDate(i);
          // convert to ISO, Monday is 0 and 6 is Sunday
          var weekDay = ( tmpDate.getDay() + 6 ) % 7;
          var colIndex = ( weekDay - this._firstWeekDay + 7 ) % 7;
          if (tmpDate.getMonth() == this._calendarDate.getMonth())
          {

              var isToday = this._datesAreSame(tmpDate, today);

              cells[currentWeek][colIndex] = { text: "", className: "" };

              if (this._datesAreSame(this._selectedDate, tmpDate)) 
                  cells[currentWeek][colIndex].className += "selected ";
              if (isToday)
                  cells[currentWeek][colIndex].className += "today ";
              if (( tmpDate.getDay() + 6 ) % 7 == this._redWeekDay) // ISO
                  cells[currentWeek][colIndex].className += "red";

              cells[currentWeek][colIndex].text =
              this._matrix[currentWeek][colIndex] = tmpDate.getDate();

              if (colIndex == 6)
                  currentWeek++;
          }
      }

      // fix day letter order if not standard
      var weekDays = this._days;
      if (this._firstWeekDay != 0)
      {
          weekDays = new Array(7);
          for (i = 0; i < 7; i++)
              weekDays[i] = this._days[ (i + this._firstWeekDay) % 7];
      }

      // update text in days row
      var tds = this._table.firstChild.tBodies[0].rows[0].cells;
      for (i = 0; i < cols; i++)
          tds[i].firstChild.data = weekDays[i];

      // update the text nodes and class names
      var trs = this._table.firstChild.tBodies[0].rows;
      var tmpCell;
      var nbsp = String.fromCharCode(160);
      for (var y = 0; y < rows; y++)
      {
          for (var x = 0; x < cols; x++)
          {
              tmpCell = trs[y + 2].cells[x];
              if (typeof cells[y][x] != "undefined")
              {
                  tmpCell.className = cells[y][x].className;
                  tmpCell.firstChild.data = cells[y][x].text;
              }
              else
              {
                  tmpCell.className = "";
                  tmpCell.firstChild.data = nbsp;
              }
          }
      }
  }

  // sets the label showing the year and selected month
  DatePicker.prototype._setTopLabel = function ()
  {
      var str = this._calendarDate.getFullYear() + " " + this._months[ this._calendarDate.getMonth() ];
      if (this._topLabel != null)
          this._topLabel.lastChild.data = str;
  }

  DatePicker.prototype.goToNextMonth = function ()
  {
      var d = new Date(this._calendarDate);
      d.setDate(Math.min(d.getDate(), DatePicker.getDaysPerMonth(d.getMonth() + 1,
              d.getFullYear()))); // no need to catch dec -> jan for the year
      d.setMonth(d.getMonth() + 1);
      this.setCalendarDate(d);
  }

  DatePicker.prototype.goToPreviousMonth = function ()
  {
      var d = new Date(this._calendarDate);
      d.setDate(Math.min(d.getDate(), DatePicker.getDaysPerMonth(d.getMonth() - 1,
              d.getFullYear()))); // no need to catch jan -> dec for the year
      d.setMonth(d.getMonth() - 1);
      this.setCalendarDate(d);
  }

  DatePicker.prototype.goToToday = function ()
  {
      //note: small tweak here so that clicking the "Today" button will properly update the selected date and trigger selected
      //but note that we want this behavior iff "today" is already selected and visible. 
      //For instance: If you're looking at some date months away from today and want to jump back to today AND today is the selectedDate
      //then we don't want that to close the calendar.
      var today = new Date();
      var forceOnSelect=false;
      if (this._selectedDate == null || (this._datesAreSame(today, this._selectedDate) && this._calendarDate.getMonth() == today.getMonth() && this._calendarDate.getFullYear() == today.getFullYear())) {
          //then go ahead and force the selection...
          forceOnSelect=true;
      }
      this.setDate(new Date(), forceOnSelect);//note that setDate calls setCalendarDate...
  }

  DatePicker.prototype.setShowToday = function (bShowToday)
  {
      if (typeof bShowToday == "string")
          bShowToday = !/false|0|no/i.test(bShowToday);

      if (this._todayButton != null)
          this._todayButton.style.visibility = bShowToday ? "visible" : "hidden";
      this._showToday = bShowToday;
  }

  DatePicker.prototype.getShowToday = function ()
  {
      return this._showToday;
  }

  DatePicker.prototype.setShowNone = function (bShowNone)
  {
      if (typeof bShowNone == "string")
          bShowNone = !/false|0|no/i.test(bShowNone);

      if (this._noneButton != null)
          this._noneButton.style.visibility = bShowNone ? "visible" : "hidden";
      this._showNone = bShowNone;
  }

  DatePicker.prototype.getShowNone = function ()
  {
      return this._showNone;
  }

  // 0 is monday and 6 is sunday as in the ISO standard
  DatePicker.prototype.setFirstWeekDay = function (nFirstWeekDay)
  {
      if (this._firstWeekDay != nFirstWeekDay)
      {
          this._firstWeekDay = nFirstWeekDay;
          this._updateTable();
      }
  }

  DatePicker.prototype.getFirstWeekDay = function ()
  {
      return this._firstWeekDay;
  }

  // 0 is monday and 6 is sunday as in the ISO standard
  DatePicker.prototype.setRedWeekDay = function (nRedWeekDay)
  {
      if (this._redWeekDay != nRedWeekDay)
      {
          this._redWeekDay = nRedWeekDay;
          this._updateTable();
      }
  }

  DatePicker.prototype.getRedWeekDay = function ()
  {
      return this._redWeekDay;
  }

  
  DatePicker.prototype.setLocalizations = function(monthNames, dayNames, todayLabel, noneLabel)
  {
      this._months = monthNames;
      this._days = dayNames;
      this._todayLabel = todayLabel;
      this._noneLabel = noneLabel;
      if (this._todayButton != null)
      {
          this._todayButton.innerHTML = todayLabel;
      }
      if (this._noneButton != null)
      {
          this._noneButton.innerHTML = noneLabel;
      }
      this._updateTable();
  }

  DatePicker.prototype._showLabelPopup = function ()
  {

      var dateContext = function (dp, d)
      {
          return function (e)
          {
              dp._hideLabelPopup();
              dp.setCalendarDate(d);
              return false;
          };
      };

      var dp = this;

      // clear all old elements in the popup
      while (this._labelPopup.hasChildNodes())
          this._labelPopup.removeChild(this._labelPopup.firstChild);

      var a, tmp, tmp2;
      for (var i = -3; i < 4; i++)
      {
          tmp = new Date(this._calendarDate);
          tmp2 = new Date(this._calendarDate);    // need another tmp to catch year change when checking leap
          tmp2.setDate(1);
          tmp2.setMonth(tmp2.getMonth() + i);
          tmp.setDate(Math.min(tmp.getDate(), DatePicker.getDaysPerMonth(tmp.getMonth() + i,
                  tmp2.getFullYear())));
          tmp.setMonth(tmp.getMonth() + i);

          a = this._document.createElement("a");
          a.href = "javascript:void 0;";
          a.onclick = dateContext(dp, tmp);
          a.appendChild(this._document.createTextNode(tmp.getFullYear() + " " +
                                                      this._months[ tmp.getMonth() ]));
          if (i == 0)
              a.className = "selected";
          this._labelPopup.appendChild(a);
      }

      this._topLabel.parentNode.insertBefore(this._labelPopup, this._topLabel.parentNode.firstChild);
  };

  DatePicker.prototype._hideLabelPopup = function ()
  {
      if (this._labelPopup.parentNode)
          this._labelPopup.parentNode.removeChild(this._labelPopup);
  };

  DatePicker.prototype._datesAreSame = function(d1,d2)
  {
      if (d1 == null && d2 == null)
          return true;
      else if (d1 == null)
          return false;
      else if (d2 == null)
          return false;
      return d1.getDate() == d2.getDate() && d1.getMonth() == d2.getMonth() && d1.getFullYear() == d2.getFullYear();    
  }

  DatePicker._daysPerMonth = [31,28,31,30,31,30,31,31,30,31,30,31];
  DatePicker.getDaysPerMonth = function (nMonth, nYear)
  {
      nMonth = (nMonth + 12) % 12;
      var res = DatePicker._daysPerMonth[nMonth];
      if (nMonth == 1)
      {
          res += nYear % 4 == 0 && !(nYear % 400 == 0) ? 1 : 0;
      }
      return res;
  };
  export default DatePicker;

