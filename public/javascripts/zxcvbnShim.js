window.addEventListener('DOMContentLoaded', function() {
  let strength = {
    0: "Worst",
    1: "Bad",
    2: "Weak",
    3: "Good",
    4: "Strong"
  };

  let password = document.querySelector('[data-pwd="true"]');
  let meter = document.querySelector('#password-strength-meter');
  let msg = document.querySelector('#password-strength-text');

  function showFeedback() {
    let val = password.value;
    let result = zxcvbn(val);

    // Update the password strength meter
    meter.value = result.score;

    // Update the text indicator
    if (val !== "") {
      msg.textContent = "Strength: " + strength[result.score];
    } else {
      msg.textContent = "";
    }
  }

  password.addEventListener('change', showFeedback);
  password.addEventListener('keyup', showFeedback);
});