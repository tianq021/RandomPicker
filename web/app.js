const namesInput = document.getElementById("names");
const countInput = document.getElementById("count");
const splitModeSelect = document.getElementById("splitMode");
const customSplitInput = document.getElementById("customSplit");
const outputSeparatorSelect = document.getElementById("outputSeparator");
const pickButton = document.getElementById("pickButton");
const resultBox = document.getElementById("result");

function toggleCustomSplit() {
    customSplitInput.disabled = splitModeSelect.value !== "custom";
    if (customSplitInput.disabled) {
        customSplitInput.value = "";
    }
}

function buildFormBody(data) {
    const params = new URLSearchParams();
    Object.keys(data).forEach((key) => params.append(key, data[key]));
    return params;
}

function setResult(text, isError) {
    resultBox.innerText = text;
    resultBox.className = isError ? "result-names message" : "result-names";
}

async function pickNames() {
    pickButton.disabled = true;
    setResult("抽取中...", false);

    try {
        const response = await fetch("/api/pick", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            },
            body: buildFormBody({
                names: namesInput.value,
                count: countInput.value,
                splitMode: splitModeSelect.value,
                customSplit: customSplitInput.value,
                outputSeparator: outputSeparatorSelect.value,
            }),
        });

        const data = await response.json();
        if (data.success) {
            setResult(data.result, false);
        } else {
            setResult(data.message, true);
        }
    } catch (error) {
        setResult("请求失败，请检查服务是否正常运行。", true);
        console.error(error);
    } finally {
        pickButton.disabled = false;
    }
}

splitModeSelect.addEventListener("change", toggleCustomSplit);
pickButton.addEventListener("click", pickNames);
