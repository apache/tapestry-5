T5.extendInitializers({
    writeMessageTo : function(spec) {
        $(spec.id).update(spec.message);
    }
})