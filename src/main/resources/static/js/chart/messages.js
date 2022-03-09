function getSentAt(event) {
    return event.sentAt;
}

$.getJSON(apiUrl + "/groups/" + groupId + "/messages", (data) => {
    const dataArray = getDatasetByTemporalEvents(data, getSentAt);
    let ctx = document.getElementById("messagesChart");
    makeLineChart(dataArray, ctx, "Messages");
});
