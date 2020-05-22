// https://medium.com/codingtown/xterm-js-terminal-2b19ccd2a52
// https://medium.com/swlh/local-echo-xterm-js-5210f062377e
var term = new Terminal();
const consoleId = (Math.random() + 1).toString(36).substring(7);

term.open(document.getElementById('terminal'));

const prefix = "djl.ai@jconsole> ";
var input = "";

function init() {
  term.reset();
  term.write("Welcome to the simulated jconsole for DJL.\r\nThis console is equipped with:\r\nNDManager manager\r\n");
  term.write(prefix);
}

init();

term.on("data", (data) => {
  const code = data.charCodeAt(0);
  if (code == 13) { // Enter
    analyseResponse(input);
  } else if(code == 127) { // Backspace
    if (input.length > 0) {
      input = input.slice(0, input.length - 1);
      term.write("\b \b");
    }
  } else if (code < 32) { // Control
    return;
  } else { // Visible
    term.write(data);
    input += data;
  }
});

term.on("paste", (data) => {
  input += data;
  term.write(data);
});

function analyseResponse(data) {
  if (data == "clear") {
    init();
    input = "";
  }
  else {
    const Url = "http://djl-console.us-east-1.elasticbeanstalk.com/addCommand";
    fetch(Url, {
    method: "POST",
    headers: {
      'Content-Type': 'application/json; charset=UTF-8',
    },
    body: JSON.stringify({ "console_id" : consoleId, "command" : data})
    }).then(response => response.json())
    .then(data => {
      if (data["result"].length > 0) {
        var resultString = data["result"].split("\n").join("\r\n")
        term.write("\r\n" + resultString + "\r\n");
        term.write(prefix);
      } else {
        term.write("\r\n" + prefix);
      }
      input = "";
    })
    .catch((error) => {
      console.error("Error:", error)
      term.write("\r\n" + error + "\r\n");
      term.write(prefix);
      input = "";
    });
  }
}
