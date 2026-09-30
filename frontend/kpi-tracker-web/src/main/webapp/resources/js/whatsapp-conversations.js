(function (window, document) {
    'use strict';

    var positions = {
        contacts: 0,
        messageBottomGap: 0
    };
    var notificationConnection = null;
    var lastInboundMessageId = null;

    function contactList() {
        return document.querySelector('.conversation-list-scroll');
    }

    function messageStream() {
        return document.getElementById('conversationForm:messageStream');
    }

    window.rememberConversationScrollPositions = function () {
        var contacts = contactList();
        var messages = messageStream();

        if (contacts) {
            positions.contacts = contacts.scrollTop;
        }
        if (messages) {
            positions.messageBottomGap = messages.scrollHeight - messages.scrollTop - messages.clientHeight;
        }
    };

    window.restoreConversationScrollPositions = function () {
        var contacts = contactList();
        var messages = messageStream();

        if (contacts) {
            contacts.scrollTop = positions.contacts;
        }
        if (messages) {
            messages.scrollTop = Math.max(0, messages.scrollHeight - messages.clientHeight - positions.messageBottomGap);
        }
    };

    window.scrollConversationToBottom = function () {
        var messages = messageStream();
        if (messages) {
            messages.scrollTop = messages.scrollHeight;
        }
    };

    function notificationValue(className) {
        var element = document.querySelector('#conversationForm\\:notificationState .' + className);
        return element ? element.textContent.trim() : '';
    }

    function notificationSnapshot() {
        return {
            connection: notificationValue('notification-connection'),
            id: notificationValue('notification-message-id'),
            sender: notificationValue('notification-sender'),
            body: notificationValue('notification-body')
        };
    }

    function setNotificationBaseline() {
        var snapshot = notificationSnapshot();
        notificationConnection = snapshot.connection;
        lastInboundMessageId = snapshot.id;
        updateNotificationPermissionUi();
    }

    function updateNotificationPermissionUi() {
        var panel = document.getElementById('notificationPermissionPanel');
        var message = document.getElementById('notificationPermissionMessage');
        var button = document.getElementById('enableNotificationButton');
        if (!panel || !message || !button) {
            return;
        }

        panel.classList.remove('permission-granted', 'permission-denied');
        if (!('Notification' in window)) {
            message.textContent = 'This browser does not support desktop notifications.';
            button.style.display = 'none';
        } else if (Notification.permission === 'granted') {
            panel.classList.add('permission-granted');
            message.textContent = 'Notifications are enabled for this browser.';
            button.style.display = 'none';
        } else if (Notification.permission === 'denied') {
            panel.classList.add('permission-denied');
            message.textContent = 'Notifications are blocked. Open the browser site settings, allow Notifications for this site, then reload the page.';
            button.style.display = 'none';
        } else {
            message.textContent = 'Click Enable notifications, then choose Allow in the browser prompt.';
            button.style.display = '';
        }
    }

    window.requestWhatsappNotificationPermission = function () {
        if (!('Notification' in window)) {
            window.alert('This browser does not support desktop notifications.');
            return;
        }
        Notification.requestPermission().then(function (permission) {
            updateNotificationPermissionUi();
            if (permission === 'granted') {
                new Notification('egoSms notifications enabled', {
                    body: 'You will be notified when a new WhatsApp message arrives.',
                    tag: 'egosms-notifications-enabled'
                });
            }
        }).catch(function () {
            updateNotificationPermissionUi();
        });
    };

    window.checkForNewWhatsappMessage = function () {
        var snapshot = notificationSnapshot();

        if (snapshot.connection !== notificationConnection) {
            notificationConnection = snapshot.connection;
            lastInboundMessageId = snapshot.id;
            return;
        }
        if (!snapshot.id || snapshot.id === lastInboundMessageId) {
            return;
        }

        lastInboundMessageId = snapshot.id;
        if ('Notification' in window && Notification.permission === 'granted') {
            var notification = new Notification('New WhatsApp message from ' + (snapshot.sender || 'a customer'), {
                body: snapshot.body || 'Open egoSms to view the message.',
                tag: 'whatsapp-' + snapshot.id
            });
            notification.onclick = function () {
                window.focus();
                notification.close();
            };
        }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setNotificationBaseline);
    } else {
        setNotificationBaseline();
    }
}(window, document));
