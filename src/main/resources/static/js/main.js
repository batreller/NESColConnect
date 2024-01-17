'use strict';


const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
// const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const connectingElement = document.querySelector('.connecting');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let nickname = null;
let fullname = null;
let selectedUserId = null;
var firstUser = null;

function randomString(length) {
    const characters = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';

    for (let i = 0; i < length; i++) {
        const randomIndex = Math.floor(Math.random() * characters.length);
        result += characters.charAt(randomIndex);
    }

    return result;
}

let webSocketSessionId = randomString(50);
var publicKeys = {};
var anotherGuyPublicKey = {};
var privateKey = null;
var chatId = null;
function connectToWebsocket(token) {
    let socket = new SockJS('/ws', [], {
        sessionId: () => {
            return webSocketSessionId
        }
    });


    // const socket = new SockJS('/ws');
    // socket.sessionId = randomString(8)
    console.log("webSocketSessionId -> ", webSocketSessionId)
    stompClient = Stomp.over(socket);
    chatArea.innerHTML = '';
    makeButtonUnclickable("stopBtn")
    makeButtonUnclickable("findStudent")
    makeVisible("chatSearching")
    makeInvisible("chatClosed")
    firstUser = true;

    stompClient.connect({"token": token}, onConnected, onError);
}

function closeWebsocketConnection(event) {
    stompClient.disconnect(function() {
        console.log('Disconnected');
    });

    chatClosed(null);
}
function onConnected(options) {
    // dialog found

    stompClient.subscribe(`/user/${webSocketSessionId}/private/student.found`, onDialogFound);
    stompClient.subscribe(`/user/${webSocketSessionId}/private/student.not.found`, onDialogNotFound);

    // stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived);
    // stompClient.subscribe('/topic/public', function (response) {
    //     console.log('/topic/public Received message:', response.body);
    // });
    stompClient.subscribe(`/user/${webSocketSessionId}/queue/private`, function (response) {
        console.log('/user/webSocketSessionId/queue/private Received message:', response.body);
    });
    stompClient.send("/app/chat/search", {},
        JSON.stringify({a: "textt"})
    );
}

function onDialogNotFound(message) {
    firstUser = false;
    console.log(message);
}

function arrayBufferToBase64(buffer) {
    const binary = new Uint8Array(buffer);
    return btoa(String.fromCharCode(...binary));
}

function base64ToArrayBuffer(base64) {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);

    for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }

    return bytes.buffer;
}

async function keyToBase64(cryptoKey) {
    const exportedKey = await crypto.subtle.exportKey('spki' || 'pkcs8', cryptoKey);
    return btoa(String.fromCharCode.apply(null, new Uint8Array(exportedKey)));
}

async function base64ToKey(base64Key) {
    const binaryKey = atob(base64Key);
    const arrayBufferKey = new Uint8Array(binaryKey.length);

    for (let i = 0; i < binaryKey.length; ++i) {
        arrayBufferKey[i] = binaryKey.charCodeAt(i);
    }

    return await crypto.subtle.importKey(
        'spki',
        arrayBufferKey.buffer,
        {name: 'RSA-OAEP', hash: {name: 'SHA-256'}},
        true,
        ['encrypt']
    );
}
async function generateKeyPair() {
    try {
        var keyPair = await crypto.subtle.generateKey(
            {
                name: 'RSA-OAEP',
                modulusLength: 2048,
                publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
                hash: { name: 'SHA-256' },
            },
            true,
            ['encrypt', 'decrypt']
        );

        var publicKey = await crypto.subtle.exportKey('spki', keyPair.publicKey);
        var privateKey = await crypto.subtle.exportKey('pkcs8', keyPair.privateKey);

        // Import public and private keys as CryptoKey objects
        const importedPublicKey = await crypto.subtle.importKey(
            'spki',
            publicKey,
            { name: 'RSA-OAEP', hash: { name: 'SHA-256' } },
            true,
            ['encrypt']
        );

        const importedPrivateKey = await crypto.subtle.importKey(
            'pkcs8',
            privateKey,
            { name: 'RSA-OAEP', hash: { name: 'SHA-256' } },
            true,
            ['decrypt']
        );

        console.log('Public Key:\n', importedPublicKey);
        console.log('\nPrivate Key:\n', importedPrivateKey);

        return {
            publicKey: importedPublicKey,
            privateKey: importedPrivateKey,
        };
    } catch (error) {
        console.error('RSA key pair generation error:', error);
    }
}

async function onDialogFound(message) {
    var jsonMesssage = JSON.parse(message.body);
    chatId = jsonMesssage.chatId
    // localStorage.setItem('chatId', jsonMesssage.chatId);

    stompClient.subscribe(`/user/${webSocketSessionId}/private/chat.acceptance`, onDialogAcceptance)
    // stompClient.subscribe(`/user/${webSocketSessionId}/private/chat.not.accepted`, onDialogNotAccepted)
    // stompClient.unsubscribe(`/chat/${webSocketSessionId}/private/student.found`)
    var keyPair = await generateKeyPair();
    console.log(keyPair);

    // localStorage.setItem('privateKey', arrayBufferToBase64(keyPair.privateKey))
    privateKey = keyPair.privateKey;

    stompClient.send(`/app/chat/accept`, {}, JSON.stringify({'chatId': chatId, 'publicKey': await keyToBase64(keyPair.publicKey)}))
    // stompClient.unsubscribe(`/user/${webSocketSessionId}/private/student.found`)
    // makeVisible("messageForm")
    // todo make send mesasge field visible etc
}

function separateDigits(input, groupSize) {
    const regex = new RegExp(`.{1,${groupSize}}`, 'g');
    const groups = input.match(regex);
    return groups.join(` + ` + '\n');
}

async function onDialogAcceptance(message) {
    var jsonMesssage = JSON.parse(message.body);

    if (jsonMesssage.isAccepted === true) {
        console.log('chat was accepted');

        publicKeys = {}
        for (var sessionId in jsonMesssage.publicKeys) {
            if (sessionId !== webSocketSessionId) {
                anotherGuyPublicKey = await base64ToKey(jsonMesssage.publicKeys[sessionId]);
            }
            publicKeys[sessionId] = await base64ToKey(jsonMesssage.publicKeys[sessionId]);
        }
        var qrcodearea = document.getElementById('qrcode-area')
        if (firstUser) {
            var validateSecureCode = await keyToBase64(publicKeys[webSocketSessionId]) + await keyToBase64(anotherGuyPublicKey)
        } else {
            var validateSecureCode = await keyToBase64(anotherGuyPublicKey) + await keyToBase64(publicKeys[webSocketSessionId])
        }
        validateSecureCode = await hashString(validateSecureCode);
        var qrcodeareaa = document.getElementById('qrcode-area')

        makeVisible('qrcode-area')
        const midpoint = Math.floor(validateSecureCode.length / 2);
        const firstHalf = validateSecureCode.substring(0, midpoint);
        const secondHalf = validateSecureCode.substring(midpoint);

        qrcodearea.innerHTML = `<h3>Validate that codes are same for you and another student to be 100% sure that your chat is not being intercepted</h3><br><br>${separateDigits(firstHalf, 8)}<br>${separateDigits(secondHalf, 8)}<br><br>`;
        var qrcode = new QRCode(qrcodeareaa, {
            text: validateSecureCode,
            width: 128,
            height: 128
        });

        // generateKeyPair();
        // var key = {
        //     'chatId': chatId,
        //     'publicKey': publicKey
        // }
        // stompClient.send(`/chat/key/send`, {}, JSON.stringify(key))

        makeButtonClickable("stopBtn")
        makeInvisible("chatSearching")

        makeVisible("messageForm");
        makeVisible("message-input");
        // stompClient.subscribe(`/user/${webSocketSessionId}/chat/${chatId}/key`, keyReceived)
        stompClient.subscribe(`/chat/${chatId}/message`, displayMessage)
        stompClient.subscribe(`/chat/${chatId}/closed`, chatClosed)
    } else {
        console.log('chat was NOT accepted, looking for chat again');
        stompClient.disconnect(function () {
            console.log('Disconnected');
        });
        connectToWebsocket(localStorage.getItem('token'));
    }
}

function chatClosed(message) {
    makeButtonClickable("findStudent");
    makeButtonUnclickable("stopBtn");

    makeInvisible("message-input")
    makeVisible("chatClosed")
}
async function findAndDisplayConnectedUsers() {
    const connectedUsersResponse = await fetch('/users');
    let connectedUsers = await connectedUsersResponse.json();
    connectedUsers = connectedUsers.filter(user => user.nickName !== nickname);
    const connectedUsersList = document.getElementById('connectedUsers');
    connectedUsersList.innerHTML = '';

    connectedUsers.forEach(user => {
        appendUserElement(user, connectedUsersList);
        if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
            const separator = document.createElement('li');
            separator.classList.add('separator');
            connectedUsersList.appendChild(separator);
        }
    });
}

function appendUserElement(user, connectedUsersList) {
    const listItem = document.createElement('li');
    listItem.classList.add('user-item');
    listItem.id = user.nickName;

    const userImage = document.createElement('img');
    userImage.src = '../img/favicon.ico';
    userImage.alt = user.fullName;

    const usernameSpan = document.createElement('span');
    usernameSpan.textContent = user.fullName;

    const receivedMsgs = document.createElement('span');
    receivedMsgs.textContent = '0';
    receivedMsgs.classList.add('nbr-msg', 'hidden');

    listItem.appendChild(userImage);
    listItem.appendChild(usernameSpan);
    listItem.appendChild(receivedMsgs);

    listItem.addEventListener('click', userItemClick);

    connectedUsersList.appendChild(listItem);
}


function userItemClick(event) {
    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
    });
    messageForm.classList.remove('hidden');

    const clickedUser = event.currentTarget;
    clickedUser.classList.add('active');

    selectedUserId = clickedUser.getAttribute('id');
    fetchAndDisplayUserChat().then();

    const nbrMsg = clickedUser.querySelector('.nbr-msg');
    nbrMsg.classList.add('hidden');
    nbrMsg.textContent = '0';

}

async function displayMessageTextForSender(messageText) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    messageContainer.classList.add('sender');
    const chatMessage = document.createElement('p');
    chatMessage.textContent = messageText;
    messageContainer.appendChild(chatMessage);
    chatArea.appendChild(messageContainer);
}

async function displayMessage(message) {
    var jsonMesssage = JSON.parse(message.body);
    var senderId = jsonMesssage.senderId;
    var content = jsonMesssage.messageText

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    if (senderId === webSocketSessionId) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }
    const chatMessage = document.createElement('p');
    chatMessage.textContent = await decryptWithPrivateKey(base64ToArrayBuffer(content), privateKey);
    messageContainer.appendChild(chatMessage);
    chatArea.appendChild(messageContainer);
}

async function fetchAndDisplayUserChat() {
    const userChatResponse = await fetch(`/messages/${nickname}/${selectedUserId}`);
    const userChat = await userChatResponse.json();
    chatArea.innerHTML = '';
    userChat.forEach(chat => {
        displayMessage(chat.senderId, chat.content);
    });
    chatArea.scrollTop = chatArea.scrollHeight;
}


function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}
async function encryptWithPublicKey(text, publicKey) {
    const encodedText = new TextEncoder().encode(text);
    return await crypto.subtle.encrypt(
        {
            name: 'RSA-OAEP',
        },
        publicKey,
        encodedText
    );
}

async function decryptWithPrivateKey(encryptedData, privateKey) {
    const decryptedData = await crypto.subtle.decrypt(
        {
            name: 'RSA-OAEP',
        },
        privateKey,
        encryptedData
    );

    const decryptedText = new TextDecoder().decode(decryptedData);
    return decryptedText;
}


async function sendMessage(event) {
    event.preventDefault();

    const unencryptedText = messageInput.value.trim();
    if (unencryptedText && stompClient) {
        var encryptedText = arrayBufferToBase64(await encryptWithPublicKey(unencryptedText, anotherGuyPublicKey))
        console.log(encryptedText)

        const chatMessage = {
            chatId: chatId,
            messageText: encryptedText,
        };
        stompClient.send(`/app/chat/message/send`, {}, JSON.stringify(chatMessage));
        // displayMessage(nickname, messageInput.value.trim());
        // messageInput.value = '';
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    await displayMessageTextForSender(unencryptedText);
    messageInput.value = '';

    event.preventDefault();
}
