function hideStuff(id) {
    document.getElementById(id).style.display = 'none';
}

function showStuff(id) {
    document.getElementById(id).style.display = 'block';
}

function loginBtn() {
    hideStuff('imgdiv');
    showStuff('sidebar');
    showStuff('menu');
}
