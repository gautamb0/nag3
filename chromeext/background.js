// Copyright (c) 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

/**
 * Get the current URL.
 *
 * @param {function(string)} callback - called when the URL of the current tab
 *   is found.
 */
var savedProfile = loadProfile();
var server = 'http://test3-djyeuxhczc.elasticbeanstalk.com/Nag';

/*chrome.identity.getAuthToken({
    interactive: true
}, function(token) {
    if (chrome.runtime.lastError) {
        alert(chrome.runtime.lastError.message);
        return;
    }
    var x = new XMLHttpRequest();
    x.open('GET', 'https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=' + token);
    x.onload = function() {

	var user_info = JSON.parse(x.response);
	user_email = user_info.email;	
    };
    x.send();
});*/



var timer = $.timer(function() {
 httpPost(server); 
 //alert('This message was sent by a timer.');
}, savedProfile.interval*1000, false);
function getCurrentTabUrl(callback) {
  // Query filter to be passed to chrome.tabs.query - see
  // https://developer.chrome.com/extensions/tabs#method-query
  var queryInfo = {
    active: true,
    currentWindow: true
  };

  chrome.tabs.query(queryInfo, function(tabs) {
    // chrome.tabs.query invokes the callback with a list of tabs that match the
    // query. When the popup is opened, there is certainly a window and at least
    // one tab, so we can safely assume that |tabs| is a non-empty array.
    // A window can only have one active tab at a time, so the array consists of
    // exactly one tab.
    var tab = tabs[0];

    // A tab is a plain object that provides information about the tab.
    // See https://developer.chrome.com/extensions/tabs#type-Tab
    var url = tab.url;

    // tab.url is only available if the "activeTab" permission is declared.
    // If you want to see the URL of other tabs (e.g. after removing active:true
    // from |queryInfo|), then the "tabs" permission is required to see their
    // "url" properties.
    console.assert(typeof url == 'string', 'tab.url should be a string');

    callback(url);
  });

  // Most methods of the Chrome extension APIs are asynchronous. This means that
  // you CANNOT do something like this:
  //
  // var url;
  // chrome.tabs.query(queryInfo, function(tabs) {
  //   url = tabs[0].url;
  // });
  // alert(url); // Shows "undefined", because chrome.tabs.query is async.
}

function backgroundFunction () {
    return "hello from the background!"
}

function httpGet(theUrl)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl+"?type=bad&user="+savedProfile.phone, true );
    xmlHttp.send( null );
}

function httpPost(theUrl)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "POST", theUrl, true );
    xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlHttp.send("type=bad&user="+savedProfile.phone);

}

function renderStatus(statusText) {
  document.getElementById('status').textContent = statusText;
}

//document.addEventListener('DOMContentLoaded', function() {
//  getCurrentTabUrl(function(url) {
//    // Put the image URL in Google search.
//	if(url.includes("reddit"))
//	{
//      		
//		httpGet('http://localhost:8080/','bad', renderStatus);
//		
//  	}
//   	else
//	{
//		chrome.tabs.executeScript({
//    code: 'document.body.style.backgroundColor="red"'
//  });
//	}

//  });
//});


//chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
//   alert(changeInfo.url);
//}); 

chrome.windows.onFocusChanged.addListener(function(windowID){check();});
    
chrome.tabs.onActivated.addListener(function(activeInfo) {check();
        });	


chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
	if(changeInfo.url)
{
	check();
}	
});


function check() {
    var d = new Date();
    console.log(d.getDay())
    if(!savedProfile.week[d.getDay()])
    {
	    console.log('disabled for today');
	    return;
    }
    if(parseInt(savedProfile.timeStart)>d.getHours()||parseInt(savedProfile.timeEnd)<=d.getHours())
    {
	    console.log('disabled at this time');
	    return;
    }
    if(savedProfile.enableBreak&&(parseInt(savedProfile.breakTimeStart)<=d.getHours()&&parseInt(savedProfile.breakTimeEnd)>d.getHours()))
    {
	    console.log('break time');
	    return;
    }
    console.log('checking url');
    chrome.tabs.getSelected(null,function(tab) {
    var url = tab.url;
    var bad = false;
    for(var i in savedProfile.blackList)
    {	    
    if(url.includes(savedProfile.blackList[i]))
 	{
	 bad = true;
	 break;	 
	 
 	}
    }
    if(bad)
    {
	timer.play();
    }
    else
    {
    	timer.pause();
    }
    });
}

function loadProfile() 
{
 if(typeof localStorage['profile'] !== 'undefined') {
    return JSON.parse(localStorage['profile']);
  } else {
    return saveProfile(defaultProfile());
  }
}

function saveProfile(profile) {
	localStorage['profile'] = JSON.stringify(profile);
	return profile;
}

function setProfile(profile)
{
	savedProfile = saveProfile(profile);
	return profile;
}

function defaultProfile()
{
	return {
		blackList:[
			'reddit.com',
			'imgur.com',
			'facebook.com',
			'youtube.com',
			'buzzfeed.com',
			'quora.com',
			'news.ycombinator.com',
			'instagram.com',
			'wikipedia.org',
			'slashdot.com',
			'twitter.com'
			],
		phone: '',
		interval: 15,
		week:[false,true,true,true,true,true,false],
		timeStart: '8',
		timeEnd: '18',
		breakTimeStart: '12',
		breakTimeEnd: '13',
		enableBreak: true
	}
}
