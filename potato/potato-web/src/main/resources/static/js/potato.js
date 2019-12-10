<!--  potato list -->


function pad(number, length) {
    var str = "" + number
    while (str.length < length) {
        str = '0' + str
    }
    return str
}

Date.prototype.plusHours = function (hours) {
    var mm = this.getMonth() + 1; // getMonth() is zero-based
    var dd = this.getDate();
    var hh = this.getHours() + hours;
    var mi = this.getMinutes();
    var ss = this.getSeconds();

    var offset = this.getTimezoneOffset();
    offset = (offset < 0 ? '+' : '-') + pad(parseInt(Math.abs(offset / 60)), 2) + ":" + pad(Math.abs(offset % 60), 2);

    return [this.getFullYear(), "-",
        (mm > 9 ? '' : '0') + mm, "-",
        (dd > 9 ? '' : '0') + dd, "T",
        (hh > 9 ? '' : '0') + hh, ":",
        (mi > 9 ? '' : '0') + mi, ":",
        (ss > 9 ? '' : '0') + ss, offset
    ].join('');
};


var PotatoList = Vue.extend({
    template: '#potato-list',
    data() {
        return {
            searchKey: '',
            potatoes: [],
            errors: []
        }
    },
    mounted() {
        axios
            .get('/api/v1/potatoes')
            .then(response => {
            this.potatoes = response.data;
    })
    .
        catch(e => {
            this.errors.push(e);
    })
        ;
    },
    methods: {
        searchPotato: function () {
            axios.get('/api/v1/potatoes/search?keyword=' + this.searchKey)
                .then(response => {
                this.potatoes = response.data;
        })
        .catch(e => {
                this.errors.push(e)
        });
            router.push('/');
        }
    }
});

var Potato = Vue.extend({
    template: '#potato',
    data: function () {
        return {
            'potato': {}
        };
    },
    mounted() {
        axios
            .get('/api/v1/potatoes/' + this.$route.params.potato_id)
            .then(response => {
            this.potato = response.data;
    })
    .
        catch(e => {
            this.errors.push(e);
    })
        ;
    }
});


var AddPotato = Vue.extend({
    template: '#add-potato',
    data: function () {

        var rightNow = new Date();
        var later1 = rightNow.plusHours(1);
        var later2 = rightNow.plusHours(2);
        return {
            potato: {
                name: '',
                description: '',
                tags: '',
                email: '',
                priority: 1,
                duration: 1,
                timeUnit: "HOURS",
                scheduleTime: later1,
                deadline: later2
            },
            errors: []
        }
    },
    methods: {
        createPotato: function () {
            console.log("--- createPotato:" + this.potato.name + "," + this.potato.email + "," + this.potato.scheduleTime + "," + this.potato.deadline);
            axios.post('/api/v1/potatoes', this.potato)
                .then(response => {}
        )
        .
            catch(e => {
                this.errors.push(e)
        })
            router.push('/');
        }
    }
});

var PotatoEdit = Vue.extend({
    template: '#edit-potato',
    data: function () {
        return {
            potato: {},
            errors: []
        };
    },
    mounted() {
        axios
            .get('/api/v1/potatoes/' + this.$route.params.potato_id)
            .then(response => {
            this.potato = response.data;
    })
    .
        catch(e => {
            this.errors.push(e);
    })
        ;
    },
    methods: {
        updatePotato: function () {
            var potato = this.potato;
            console.log("--- updatePotato:" + this.potato);
            axios.put("/api/v1/potatoes/" + this.$route.params.potato_id, this.potato)
                .then(response => {
                console.log(response.data);
        })
        .
            catch(e => {
                this.errors.push(e)
        })
            router.push('/');
        }
    }

});

var PotatoDelete = Vue.extend({
    template: '#delete-potato',
    data: function () {
        return {
            potato: {},
            errors: []
        };
    },
    mounted() {
        axios
            .get('/api/v1/potatoes/' + this.$route.params.potato_id)
            .then(response => {
            this.potato = response.data;
    })
    .
        catch(e => {
            this.errors.push(e);
    })
        ;
    },
    methods: {
        deletePotato: function () {
            var potato = this.potato;
            console.log("--- deletePotato:" + this.potato);
            axios.delete("/api/v1/potatoes/" + this.potato.id)
                .then(response => {
                console.log(response.data);
        })
        .
            catch(e => {
                this.errors.push(e)
        })
            router.push('/');
        }
    }
});

var router = new VueRouter({
    routes: [
        {path: '/', component: PotatoList},
        {path: '/potatoes/:potato_id', component: Potato, title: 'Toast Potato'},
        {path: '/add-potato', component: AddPotato},
        {path: '/potatoes/:potato_id/edit', component: PotatoEdit, title: 'note-edit'},
        {path: '/potatoes/:potato_id/delete', component: PotatoDelete, title: 'note-delete'}
    ]
});

var app = new Vue({
    router: router
}).$mount('#app')