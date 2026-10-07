(function () {
    'use strict';
    var settings, session, authorizationCode, coexistence = false;
    var completing = false;

    var DEBUG = true;
    function log() {
        if (!DEBUG) return;
        var args = Array.prototype.slice.call(arguments);
        args.unshift('[WA-ES ' + new Date().toISOString().substr(11, 12) + ']');
        console.log.apply(console, args);
    }

    function json(response) {
        return response.text().then(function (body) {
            log('HTTP', response.status, response.url, body.substring(0, 500));
            var data;
            try { data = body ? JSON.parse(body) : {}; }
            catch (ignored) { data = { message: 'The server returned an HTML error page. Check the application log for the underlying configuration error.' }; }
            if (!response.ok) throw new Error(data.message || ('Request failed (' + response.status + ')'));
            return data;
        });
    }
    function el(id) { return document.getElementById(id); }
    function contextPath() { var holder=el('signupConfiguration'); return holder ? (holder.getAttribute('data-context-path') || '') : ''; }
    function progress(message,error) { var box=el('signupProgress'); if(!box)return; box.style.display='block'; box.style.background=error?'#fde7e7':'#e5f6ed'; box.style.color=error?'#9d2020':'#126b43'; box.textContent=message; }
    function json(response) { return response.text().then(function(body){var data;try{data=body?JSON.parse(body):{};}catch(ignored){data={message:'The server returned an HTML error page. Check the application log for the underlying configuration error.'};}if(!response.ok)throw new Error(data.message||('Request failed ('+response.status+')'));return data;}); }
    function complete() {
        if(completing||!authorizationCode||!session)return;
        var wabaId=session.waba_id||session.wabaId, phoneId=session.phone_number_id||session.phoneNumberId, businessId=session.business_id||session.businessId||'';
        if(!wabaId||(!coexistence&&!phoneId)){progress('Meta did not return the identifiers required to connect this account.',true);return;}
        completing=true; progress(coexistence?'Connecting the existing WhatsApp Business number and requesting synchronization…':'Securing the connection, subscribing webhooks and registering the number…');
        var form=new URLSearchParams();form.append('code',authorizationCode);form.append('businessId',businessId);form.append('wabaId',wabaId);form.append('coexistence',String(coexistence));if(phoneId)form.append('phoneNumberId',phoneId);
        fetch(contextPath()+'/whatsapp/signup/complete',{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},body:form.toString()})
            .then(json).then(function(){authorizationCode=null;session=null;progress(coexistence?'Existing WhatsApp Business number connected. Meta will synchronize the history and contacts approved during signup.':'WhatsApp Business Account connected successfully.');if(typeof window.refreshWhatsappAccounts==='function')window.refreshWhatsappAccounts();})
            .catch(function(error){authorizationCode=null;progress(error.message+' Start Embedded Signup again to retry.',true);})
            .then(function(){completing=false;});
    }
    function signupEvent(event) {
        if (event.origin !== 'https://www.facebook.com' && event.origin !== 'https://web.facebook.com') return;
        var message = event.data;
        log('postMessage raw from', event.origin, message);
        if (typeof message === 'string') { try { message = JSON.parse(message); } catch (ignored) { return; } }
        if (!message || message.type !== 'WA_EMBEDDED_SIGNUP') { log('ignored (not WA_EMBEDDED_SIGNUP)'); return; }
        log('WA_EMBEDDED_SIGNUP event =', message.event, 'data =', JSON.stringify(message.data));
        if (message.event === 'FINISH' || message.event === 'FINISH_WHATSAPP_BUSINESS_APP_ONBOARDING') {
            session = message.data || {};
            if (message.event === 'FINISH_WHATSAPP_BUSINESS_APP_ONBOARDING') coexistence = true;
            log('coexistence flag after event =', coexistence);
            progress('Meta signup completed. Finishing authorization…');
            complete();
        } else if (message.event === 'CANCEL') {
            progress('Signup was cancelled before completion.', true);
        } else if (message.event === 'ERROR') {
            progress((message.data && message.data.error_message) || 'Meta could not complete Embedded Signup.', true);
        }
    }
    function loadSdk() {
        window.fbAsyncInit=function(){window.FB.init({appId:settings.appId,cookie:true,xfbml:false,version:settings.apiVersion});};
        if(el('facebook-jssdk'))return;var script=document.createElement('script');script.id='facebook-jssdk';script.async=true;script.defer=true;script.crossOrigin='anonymous';script.src='https://connect.facebook.net/en_US/sdk.js';script.onerror=function(){progress('Could not load the Meta SDK. Check your network and configured JSSDK host domain.',true);};document.head.appendChild(script);
    }
    function start(mode) {
        if(!window.FB){progress('The Meta SDK is still loading. Try again in a moment.',true);return;}progress('Opening Meta Embedded Signup…');
        coexistence=mode==='coexistence';session=null;authorizationCode=null;
        var extras=coexistence?{setup:{},featureType:'whatsapp_business_app_onboarding',sessionInfoVersion:'3'}:{sessionInfoVersion:'3'};
        window.FB.login(function(response){if(response.authResponse&&response.authResponse.code){authorizationCode=response.authResponse.code;progress('Authorization received. Finishing the connection…');complete();}else if(!session)progress('Authorization was not completed.',true);},{config_id:settings.configId,response_type:'code',override_default_response_type:true,extras:extras});
    }
    function init() {
        var button=el('accountsForm:connectWhatsapp'), coexistButton=el('accountsForm:connectExistingWhatsapp');if(!button||!coexistButton)return;button.addEventListener('click',function(){start('cloud');});coexistButton.addEventListener('click',function(){start('coexistence');});window.addEventListener('message',signupEvent);
        fetch(contextPath()+'/whatsapp/signup/configuration',{credentials:'same-origin'}).then(json).then(function(value){settings=value;if(!settings.appId||!settings.configId)throw new Error('Embedded Signup is not configured in .env.');loadSdk();}).catch(function(error){button.disabled=true;coexistButton.disabled=true;progress(error.message,true);});
    }
    if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init);else init();
}());
