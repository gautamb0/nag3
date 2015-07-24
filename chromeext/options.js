formEl = document.getElementById('options-form');
background = chrome.extension.getBackgroundPage();
//emailAddrEl = document.getElementById('user');
phoneNumberEl = document.getElementById('phone');
intervalEl = document.getElementById('interval');
blackListEl = document.getElementById('blacklist');
saveSuccessfulEl = document.getElementById('save-status');
weekEl = document.querySelectorAll('.week');
timeStartEl = document.getElementById("timestart");
timeEndEl = document.getElementById("timeend");
breakTimeStartEl = document.getElementById("breaktimestart");
breakTimeEndEl = document.getElementById("breaktimeend");
enableBreakEl = document.getElementById("enablebreak");

var weekArr = [];

//emailAddrEl.value = background.loadProfile()['email'];
phoneNumberEl.value = background.loadProfile()['phone'];
intervalEl.value = background.loadProfile()['interval'];
blackListEl.value = background.loadProfile()['blackList'].join("\n");
enableBreakEl.checked = background.loadProfile()['enableBreak'];

populateTime(timeStartEl,background.loadProfile()['timeStart']);
populateTime(timeEndEl,background.loadProfile()['timeEnd']);
populateTime(breakTimeStartEl,background.loadProfile()['breakTimeStart']);
populateTime(breakTimeEndEl,background.loadProfile()['breakTimeEnd']);

if(!enableBreakEl.checked)
{
	breakTimeStartEl.disabled = true;
	breakTimeEndEl.disabled = true;
}

function toggleBreak(event)
{
	console.log("checkbox changed");
	if(!enableBreakEl.checked)
	{	
		breakTimeStartEl.disabled = true;
		breakTimeEndEl.disabled = true;
	}
	else
	{
		breakTimeStartEl.disabled = false;
		breakTimeEndEl.disabled = false;
	}
}

function populateTime(selectElement, selectedVal)
{
for(i = 0; i < 25; i++)
{
	var hour;
	var str = "am";
	if(i==0||i==24)
	{
		hour = 12;
	}
	else if(i==12)
	{
		hour = 12;
		str = "pm";
	}
	else if(i>12&&i<24)
	{
		hour = i - 12;
		str = "pm";
	}
	else
	{
		hour = i;
	}
	var option = document.createElement("option");
	option.text = hour+":00 "+str;
	option.value = i;
	if(i==parseInt(selectedVal))
		option.selected = 'selected';
	selectElement.add(option);
	
}
}
for(i = 0; i < weekEl.length; ++i)
{
	weekEl[i].checked = background.loadProfile()['week'][i]
		
}

function submitOptions(event){
	
	event.preventDefault();

	var phoneChanged = false;
	var xmlHttp = new XMLHttpRequest();
	var theUrl = 'http://test3-djyeuxhczc.elasticbeanstalk.com/UpdateProfile'
	if(!validateInterval(intervalEl.value))
	{
		saveSuccessfulEl.innerHTML = 'Bad interval. Please enter a time in seconds between 10 and 600.';
		return;
	}
	if(!validateList(blackListEl.value))
	{
		saveSuccessfulEl.innerHTML = 'Bad blacklist. Please enter a valid pattern to match against on each line.';
		return;
	}
	if(!validatePhone(phoneNumberEl.value))
	{
		saveSuccessfulEl.innerHTML = 'Bad phone number. Please enter a valid 10 or 11 digit US phone number.';
		return;
	}
	if(!validateTimes(parseInt(timeStartEl.value),parseInt(timeEndEl.value),parseInt(breakTimeStartEl.value),parseInt(breakTimeEndEl.value),enableBreakEl.checked))
	{
		saveSuccessfulEl.innerHTML = 'Bad time interval. Please select a valid range for a start and end time, and a valid break within if enabled.';
		return;
	}
	console.log("Old phone number "+background.loadProfile()['phone']+" new phone number "+phoneNumberEl.value);
	xmlHttp.open( "POST", theUrl, true );
   	xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	xmlHttp.send( "user="+phoneNumberEl.value);//+"&user="+emailAddrEl.value);
	phoneChanged = phoneNumberEl.value!=background.loadProfile()['phone'];
	
	for(i = 0; i < weekEl.length; ++i)
	{
		weekArr[i] = weekEl[i].checked;	
	}

	background.setProfile({
		blackList: blackListEl.value.split(/\r?\n/),
		//email: emailAddrEl.value,
		phone: phoneNumberEl.value,
		interval: intervalEl.value,
		week: weekArr.slice(),
		timeStart: timeStartEl.value,
		timeEnd: timeEndEl.value,
		breakTimeStart: breakTimeStartEl.value,
		breakTimeEnd: breakTimeEndEl.value,
		enableBreak: enableBreakEl.checked
	});
	if(phoneChanged)
	{
		saveSuccessfulEl.innerHTML = 'Options successfully saved. You will receive a text message to confirm your phone number.';
	}
	else
	{
		saveSuccessfulEl.innerHTML = 'Options successfully saved.';
	}
	chrome.extension.getBackgroundPage().window.location.reload();
	console.log("submitted options");
	console.log(timeStartEl);
}

function validatePhone(phoneNumber)
{
	var tempStr = phoneNumber.replace(/(\-|\(|\))/g,"");
	console.log(tempStr);
	console.log(tempStr.length);
	if(isNaN(tempStr))
	{
		return false;
	}	
	if(tempStr.length==11)
	{
		phoneNumberEl.value = tempStr;
		return true;
	}
	if(tempStr.length==10)
	{
		phoneNumberEl.value = "1"+ tempStr;
		return true;
	}
	return false;
}

function validateList(listRaw)
{
	var listSplit = listRaw.split(/\r?\n/);
	var good = true;
	for(var i in listSplit)
	{
		if(listSplit[i] == "")
		{
			good = false;
		}
	}
	return good;
}

function validateInterval(intervalNumber)
{
	return (intervalNumber >= 10 && intervalNumber <= 600);
}

function validateTimes(start, end, breakStart, breakEnd, breakEnabled)
{
	console.log("Start time "+start+" end time "+end+" breakStart "+breakStart+" breakEnd "+breakEnd);
	return(start<end&&((breakStart<breakEnd&&start<breakStart&&breakEnd<end)||!breakEnabled));

}
formEl.addEventListener('submit', submitOptions, false);

enableBreakEl.addEventListener('change', toggleBreak);
