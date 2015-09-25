package demo.controller;

import demo.model.CBRInfo;
import demo.model.Info;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ws on 25.09.15.
 */
@RestController
@RequestMapping(value = "/api/rate", method = RequestMethod.GET)
public class Currency {

    @RequestMapping("/{code}")
    public Info currency(@PathVariable String code) throws Exception {
        return CBRInfo.get(Info.instance(code));
    }

    @RequestMapping("/{code}/{date}")
    public Info currencyForDate(@PathVariable String code, @PathVariable String date) throws Exception {
        return CBRInfo.get(Info.instance(code, date));
    }

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }
}
