<template>

<v-data-table
    :headers="headers"
    :items="view"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'View',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "ticketId", value: "ticketId" },
            { text: "status", value: "status" },
            { text: "starttime", value: "starttime" },
            { text: "endtime", value: "endtime" },
            { text: "price", value: "price" },
            { text: "reservationId", value: "reservationId" },
            { text: "userId", value: "userId" },
            { text: "userGrade", value: "userGrade" },
            { text: "discountRatio", value: "discountRatio" },
            { text: "reservationStatus", value: "reservationStatus" },
        ],
        view : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/views')

      this.view = temp.data._embedded.views;

    },
    methods: {
    }
  }
</script>

