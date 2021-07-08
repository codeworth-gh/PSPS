/* jshint esversion:6 */

/**
 * An editable list
 * @param ulId id of the editable list element (UL)
 * @param addBtnId id of the "add row" button.
 * @param callbacks callbacks for list events. Has saveRow and deleteRow
 * @return {{_emt: HTMLElement, addRow: addRow}}
 * @constructor
 */
function EditableList(ulId, addBtnId, callbacks) {
    const ul = document.getElementById(ulId);
    const sampleRow = ul.getElementsByTagName("li")[0];
    const addBtn = document.getElementById(addBtnId);

    function startEdit(rowEmt){
        const f = rowEmt.getElementsByTagName("input")[0];
        f.disabled=false;
        f.select();
        f.focus();
        rowEmt.classList.add("el-editMode");
    }
    function cancelEdit(rowEmt){
        const f = rowEmt.getElementsByTagName("input")[0];
        f.disabled=true;
        f.value = rowEmt.dataset.name;
        rowEmt.classList.remove("el-editMode");
    }

    function saveEdit(rowEmt){
        const f = rowEmt.getElementsByTagName("input")[0];
        f.disabled=true;
        rowEmt.dataset.name=f.value;
        callbacks.saveRow({id:rowEmt.dataset.id, name:rowEmt.dataset.name}).then( rowData=>{
            rowEmt.classList.remove("el-editMode");
            rowEmt.dataset.id = rowData.id;
        });

    }

    function deleteRow(rowEmt){
        if ( rowEmt.dataset.id > 1023 ) {
            swal("This is a protected item that cannot be deleted");

        } else {
            swal("Delete value " + rowEmt.dataset.name + "?", {
                dangerMode: true,
                buttons: true,
            }).then( value=>{
                if ( value ) {
                    callbacks.deleteRow(rowEmt.dataset.id).then( res=>{
                        rowEmt.remove();
                    });
                }
            });
        }
    }

    function addRow( data, goIntoEditMode ) {
        const newRow = sampleRow.cloneNode(true);
        newRow.dataset.id=data.id;
        newRow.dataset.name=data.name;
        const f = newRow.getElementsByTagName("input")[0];
        const buttons = newRow.getElementsByTagName("button");
        if ( data.id > 1023 ) {
            for (let b of buttons) {
                if ( b.dataset.elRole === "delBtn" ) {
                    b.remove();
                }
            }
        }
        const role2func = {
            editBtn: ()=>startEdit(newRow),
            delBtn:  ()=>deleteRow(newRow),
            commitBtn: ()=>saveEdit(newRow),
            cancelBtn: ()=>cancelEdit(newRow)
        };
        for (let b of buttons) {
            const handler = role2func[b.dataset.elRole];
            if ( handler ) b.addEventListener("click", handler);
        }

        f.value=data.name;
        f.addEventListener("keyup", function(event) {
            // Number 13 is the "Enter" key on the keyboard
            if (event.key === "Enter") {
                event.preventDefault();
                saveEdit(newRow);
            } else if ( event.key==="Escape" ) {
                event.preventDefault();
                cancelEdit(newRow);
            }
        });
        ul.appendChild(newRow);

        if ( goIntoEditMode ) startEdit(newRow);
    }

    // setup
    sampleRow.remove();
    addBtn.addEventListener("click", ()=>addRow({id:0, name:""}, true));

    return {
        _emt: ul,
        addRow:addRow
    };
}