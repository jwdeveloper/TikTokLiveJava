var dataType = "messages"
var connected = false;
var paginationIndex = 0;
var maxPages = 10;
var pages = [];
var userName = "";

var port = 8002;
var baseUrl = `http://localhost:${port}`

var data =
    {
        dataType: "event",
        dataName: "",
        user: "",
        sessionTag: "",
        page: 0,

        collector:
            {
                connected: false,
                user: "",
                sessionTag: "",
            }
    }


dropDown("usersDropDown", () => `${baseUrl}/tiktok/users`, async (e) => {
    data.user = e;
    update();

})

dropDown("dataTypeDropDown", () => `${baseUrl}/tiktok/data-types`, async (e) => {
    data.dataType = e;
    update();
})

dropDown("sessionTagDropDown", () => `${baseUrl}/tiktok/sessions?user=${data.user}`, async (e) => {
    data.sessionTag = e;
    update();
})


function update() {
    new Promise(async function (resolve, reject) {
        await updateAsync();
        resolve(true);
    });
}

async function updateAsync() {
    console.log("Updating", data);
    await updateConnectionButton()
    await updateDataNamesList(async (dataName) => {
        data.dataName = dataName;
        data.page = 0;
        await updateContent(`${baseUrl}/tiktok/data?name=${data.dataName}&type=${data.dataType}&page=${data.page}`);
        await updatePagination(async (page, link) => {
            data.page = page;
            await updateContent(link)
        });
    });
    await fetch(`${baseUrl}/tiktok/update?user=${data.user}&session=${data.sessionTag}`);
}

async function updatePagination(onSelect) {
    let response = await fetch(`${baseUrl}/tiktok/data/pages?name=${data.dataName}&type=${data.dataType}`);
    let json = await response.text();
    let object = JSON.parse(json);
    let pages = object.links;
    $("#pages").empty();
    let page = 0;
    $.each(pages, function (index, element) {

        let currentPage = page;
        page++;
        let content = $('<button>', {
            class: 'btn btn-primary margin-left',
            text: index
        }).click(async function () {

            await onSelect(currentPage, element);
        });
        $("#pages").append(content);
    });
}

async function updateContent(link) {
    console.log("updating content", data)
    let response = await fetch(link);
    let json = await response.text();
    console.log(link)
    let root = JSON.parse(json);
    editor.setValue(root.content);
    $("#editor2").hide()
    if (data.dataType === 'message') {
        console.log("sending proto version")
        let response2 = await fetch(`${link}&asProto=true`);
        let json2 = await response2.text();
        let root2 = JSON.parse(json2);
        editor2.setValue(root2.content);
        $("#editor2").show()
    }

    if (data.dataType === 'response' && data.dataName === 'Http') {

        var content = JSON.parse(root.content);
        var body = JSON.parse(content.request.body)
        var asJson = JSON.stringify(body, null, 2)
        editor2.setValue(asJson);
        $("#editor2").show()
    }

}

async function updateDataNamesList(onSelect) {
    let response = await fetch(`${baseUrl}/tiktok/data/names?type=${data.dataType}`);
    let json = await response.text();
    let responce = JSON.parse(json);
    let element = $("#dataList");
    console.log(responce)
    element.empty();
    $.each(responce, function (index, dataName) {
        let listItem = $('<li>', {
            class: 'list-group-item',
            text: dataName
        }).click(async function () {
            onSelect(dataName)
        });
        element.append(listItem);
    });
}

function dropDown(elementId, onUrl, onSelect) {
    let dropDown = $("#" + elementId);
    dropDown.on('show.bs.dropdown', async function (e, b) {
        let response = await fetch(onUrl());
        let json = await response.text();
        let values = JSON.parse(json);
        let optionsElement = dropDown.find("div")
        optionsElement.empty();
        let displayElement = dropDown.find("button")
        for (let value of values) {
            let dropDownItem = $('<p>', {
                class: 'dropdown-item',
                text: value
            }).click(async function () {
                displayElement.text(value)
                onSelect(value)
            });
            optionsElement.append(dropDownItem)
        }
    })
}

setInterval(() => {
    new Promise(async (a, b) => {
        await updateConnectionButton()
    });
}, 1000)


$("#connectionButton").on('click', async (a) => {

    if (!data.collector.connected) {
        console.log("connecting")
        await connect()
    } else {
        console.log("disconnecting")
        await disconnect()
    }
});

async function updateConnectionButton() {
    let button = $("#connectionButton");
    let response = await fetch(`${baseUrl}/tiktok/status`);
    let json = await response.text();
    let values = JSON.parse(json);
    console.log(values)
    data.collector.connected = values;
    if (data.collector.connected) {
        button.text("disconnect");
    } else {
        button.text("connect");
    }

}

async function connect() {
    let name = document.getElementById('name').value;
    let session = document.getElementById('sessionTag').value;
    data.collector.name = name
    data.collector.sessionTag = session

    let response = await fetch(`${baseUrl}/tiktok/connect?name=${data.collector.name}&session=${data.collector.sessionTag}`);
    let greeting = await response.text();
}

async function disconnect() {
    let response = await fetch(`${baseUrl}/tiktok/disconnect`);
    let greeting = await response.text();
}

update()